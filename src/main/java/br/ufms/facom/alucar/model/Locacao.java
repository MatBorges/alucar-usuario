package br.ufms.facom.alucar.model;

import br.ufms.facom.alucar.util.ParametrosSistema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entidade de domínio representando o contrato de locação de veículo (RF08).
 * Segue o padrão GRASP Information Expert.
 */
public class Locacao {

    /** Zero monetario, sempre com duas casas. */
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private Integer idLocacao;
    private LocalDateTime dataRetirada;
    private LocalDate dataDevolucaoPrevista;
    private int kmInicial;
    private BigDecimal valorEstimado;
    private BigDecimal valorCaucao;
    private StatusLocacao status = StatusLocacao.ATIVA;

    // Preenchidos somente no caso de uso Registrar Devolucao (RF09).
    // Os nomes seguem os atributos citados no contrato de operacao.
    private LocalDateTime dataHoraDevolucaoEfetiva;
    private Integer kmFinal;
    private BigDecimal valorFinal;
    private String vistoria;
    private BigDecimal custoAvaria;

    private Cliente cliente;
    private Veiculo veiculo;
    private Usuario atendente;

    public Locacao() {
        this.dataRetirada = LocalDateTime.now();
    }

    public Locacao(Integer idLocacao, LocalDateTime dataRetirada, LocalDate dataDevolucaoPrevista,
                   int kmInicial, BigDecimal valorEstimado, BigDecimal valorCaucao,
                   StatusLocacao status, Cliente cliente, Veiculo veiculo, Usuario atendente) {
        this.idLocacao = idLocacao;
        this.dataRetirada = dataRetirada != null ? dataRetirada : LocalDateTime.now();
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.kmInicial = kmInicial;
        this.valorEstimado = valorEstimado;
        this.valorCaucao = valorCaucao;
        this.status = status != null ? status : StatusLocacao.ATIVA;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.atendente = atendente;
    }

    public boolean isAtiva() {
        return status == StatusLocacao.ATIVA;
    }

    // ------------------------------------------------------------------
    // Regras de negocio da locacao (GRASP Information Expert).
    // A locacao conhece suas proprias datas e o veiculo associado, por isso
    // e ela quem calcula o periodo e o valor, e nao a controladora.
    // ------------------------------------------------------------------

    /**
     * Numero de diarias do periodo contratado.
     * Um periodo que termina no mesmo dia da retirada conta como 1 diaria.
     */
    public int calcularQuantidadeDeDiarias() {
        if (dataDevolucaoPrevista == null) {
            return 0;
        }
        LocalDate inicio = dataRetirada != null
                ? dataRetirada.toLocalDate()
                : LocalDate.now();

        long diarias = ChronoUnit.DAYS.between(inicio, dataDevolucaoPrevista);
        return diarias <= 0 ? 1 : (int) diarias;
    }

    /**
     * RN05 - aplica o desconto de longa duracao sobre o valor da diaria.
     * Retorna a propria diaria quando o periodo nao atinge o minimo exigido.
     */
    public BigDecimal aplicarDescontoLongaDuracao(BigDecimal valorDiaria, int quantidadeDeDiarias) {
        if (valorDiaria == null) {
            return BigDecimal.ZERO;
        }
        if (quantidadeDeDiarias < ParametrosSistema.getDiariasParaDesconto()) {
            return valorDiaria.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal desconto = valorDiaria.multiply(
                ParametrosSistema.getPercentualDescontoLongaDuracao());

        return valorDiaria.subtract(desconto).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valor estimado do periodo: diaria do veiculo (ja com o desconto da RN05,
     * quando couber) multiplicada pela quantidade de diarias.
     */
    public BigDecimal calcularValorEstimado() {
        if (veiculo == null || veiculo.getValorDiaria() == null
                || dataDevolucaoPrevista == null) {
            return BigDecimal.ZERO;
        }
        int diarias = calcularQuantidadeDeDiarias();
        BigDecimal diariaAplicada = aplicarDescontoLongaDuracao(veiculo.getValorDiaria(), diarias);

        return diariaAplicada.multiply(BigDecimal.valueOf(diarias))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------
    // Registrar Devolucao (RF09)
    // ------------------------------------------------------------------

    /**
     * Diarias efetivamente utilizadas: da retirada ate a devolucao real.
     * Enquanto a locacao esta aberta, vale o periodo contratado.
     */
    public int calcularQuantidadeDeDiariasUtilizadas() {
        if (dataHoraDevolucaoEfetiva == null) {
            return calcularQuantidadeDeDiarias();
        }
        long diarias = ChronoUnit.DAYS.between(dataRetirada.toLocalDate(),
                dataHoraDevolucaoEfetiva.toLocalDate());
        return diarias <= 0 ? 1 : (int) diarias;
    }

    /** Dias de atraso. Zero quando devolvido no prazo ou antes dele. */
    public int calcularDiasDeAtraso() {
        if (dataHoraDevolucaoEfetiva == null || dataDevolucaoPrevista == null) {
            return 0;
        }
        long atraso = ChronoUnit.DAYS.between(dataDevolucaoPrevista,
                dataHoraDevolucaoEfetiva.toLocalDate());
        return atraso <= 0 ? 0 : (int) atraso;
    }

    /**
     * RN06 - havendo atraso, cobra-se a multa contratual fixa mais a taxa
     * diaria de atraso por cada dia excedido. Sem atraso, nao ha multa.
     */
    public BigDecimal calcularMultaPorAtraso() {
        int atraso = calcularDiasDeAtraso();
        if (atraso == 0 || veiculo == null || veiculo.getValorDiaria() == null) {
            return ZERO;
        }
        BigDecimal taxaDiaria = veiculo.getValorDiaria()
                .multiply(ParametrosSistema.getTaxaDiariaDeAtraso())
                .multiply(BigDecimal.valueOf(atraso));

        return ParametrosSistema.getMultaContratualPorAtraso()
                .add(taxaDiaria)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valor das diarias realmente usadas, com a RN05 reavaliada sobre o
     * periodo efetivo.
     *
     * O limite final evita a distorcao de a devolucao antecipada sair mais
     * cara: 29 diarias sem desconto custariam mais que 30 com desconto, entao
     * cobra-se o menor dos dois.
     */
    public BigDecimal calcularValorDasDiariasUtilizadas() {
        if (veiculo == null || veiculo.getValorDiaria() == null) {
            return ZERO;
        }
        BigDecimal diariaCheia = veiculo.getValorDiaria();
        int diarias = calcularQuantidadeDeDiariasUtilizadas();

        BigDecimal valor = aplicarDescontoLongaDuracao(diariaCheia, diarias)
                .multiply(BigDecimal.valueOf(diarias))
                .setScale(2, RoundingMode.HALF_UP);

        int limiar = ParametrosSistema.getDiariasParaDesconto();
        if (diarias < limiar) {
            BigDecimal valorNoLimiar = aplicarDescontoLongaDuracao(diariaCheia, limiar)
                    .multiply(BigDecimal.valueOf(limiar))
                    .setScale(2, RoundingMode.HALF_UP);

            if (valorNoLimiar.compareTo(valor) < 0) {
                return valorNoLimiar;
            }
        }
        return valor;
    }

    /**
     * Valor final da locacao, conforme a pos-condicao do contrato:
     * diarias do periodo efetivo + multa por atraso (RN06) + custo da avaria.
     * A caucao NAO entra aqui - ela e liberada ou retida a parte (RN03).
     */
    public BigDecimal calcularValorFinal() {
        return calcularValorDasDiariasUtilizadas()
                .add(calcularMultaPorAtraso())
                .add(custoAvaria == null ? ZERO : custoAvaria)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * RN03 - a caucao e retida ate o limite do custo da avaria; sem avaria,
     * nada e retido.
     */
    public BigDecimal calcularValorCaucaoRetido() {
        if (!possuiAvaria() || valorCaucao == null) {
            return ZERO;
        }
        return valorCaucao.min(custoAvaria).setScale(2, RoundingMode.HALF_UP);
    }

    /** RN03 - parte da caucao que volta para o cliente. */
    public BigDecimal calcularValorCaucaoADevolver() {
        BigDecimal caucao = valorCaucao == null ? ZERO : valorCaucao;
        return caucao.subtract(calcularValorCaucaoRetido())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * O que o cliente ainda paga: o valor final menos a parte da caucao que
     * ficou retida, para a avaria nao ser cobrada duas vezes.
     */
    public BigDecimal calcularValorAPagar() {
        BigDecimal aPagar = calcularValorFinal().subtract(calcularValorCaucaoRetido());
        return aPagar.signum() < 0 ? ZERO : aPagar.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean possuiAvaria() {
        return custoAvaria != null && custoAvaria.signum() > 0;
    }

    /**
     * Situacao para a qual o veiculo vai depois da devolucao, conforme as
     * pos-condicoes do contrato. A RN04 tem precedencia: veiculo depreciado
     * sai de servico mesmo que tenha avaria. Deve ser consultada DEPOIS de
     * atualizar a quilometragem do veiculo.
     */
    public StatusVeiculo situacaoDoVeiculoAposDevolucao() {
        if (veiculo == null) {
            return StatusVeiculo.DISPONIVEL;
        }
        if (veiculo.atingiuLimiteDepreciacao()) {
            return StatusVeiculo.DESATIVADO;
        }
        return possuiAvaria() ? StatusVeiculo.EM_MANUTENCAO : StatusVeiculo.DISPONIVEL;
    }

    /**
     * Calcula os valores da devolucao SEM encerrar a locacao, para a tela
     * exibir antes da confirmacao (mensagem 5 do DSS).
     */
    public ValoresDaDevolucao calcularValoresDaDevolucao(int kmFinalInformado,
                                                         BigDecimal custoAvariasInformado) {
        LocalDateTime efetivaAnterior = this.dataHoraDevolucaoEfetiva;
        Integer kmAnterior = this.kmFinal;
        BigDecimal avariaAnterior = this.custoAvaria;
        int kmVeiculoAnterior = veiculo == null ? 0 : veiculo.getKmAtual();

        try {
            this.dataHoraDevolucaoEfetiva = LocalDateTime.now();
            this.kmFinal = kmFinalInformado;
            this.custoAvaria = custoAvariasInformado == null ? ZERO : custoAvariasInformado;

            if (veiculo != null) {
                veiculo.setKmAtual(kmFinalInformado);
            }

            return new ValoresDaDevolucao(
                    calcularQuantidadeDeDiariasUtilizadas(),
                    calcularDiasDeAtraso(),
                    calcularValorDasDiariasUtilizadas(),
                    calcularMultaPorAtraso(),
                    this.custoAvaria,
                    calcularValorFinal(),
                    calcularValorCaucaoRetido(),
                    calcularValorCaucaoADevolver(),
                    calcularValorAPagar(),
                    situacaoDoVeiculoAposDevolucao());
        } finally {
            this.dataHoraDevolucaoEfetiva = efetivaAnterior;
            this.kmFinal = kmAnterior;
            this.custoAvaria = avariaAnterior;
            if (veiculo != null) {
                veiculo.setKmAtual(kmVeiculoAnterior);
            }
        }
    }

    /**
     * Encerra a locacao com os dados colhidos no atendimento e leva o veiculo
     * para a situacao de destino. Cumpre as pos-condicoes do contrato.
     * A ordem dos parametros segue a mensagem 9 do DSS.
     */
    public void registrarDevolucao(int kmFinalInformado, BigDecimal custoAvariasInformado,
                                   String descricaoVistoria) {
        this.dataHoraDevolucaoEfetiva = LocalDateTime.now();
        this.kmFinal = kmFinalInformado;
        this.custoAvaria = custoAvariasInformado == null ? ZERO : custoAvariasInformado;
        this.vistoria = descricaoVistoria;

        if (veiculo != null) {
            veiculo.atualizarQuilometragem(kmFinalInformado);
            veiculo.alterarStatus(situacaoDoVeiculoAposDevolucao());
        }

        this.valorFinal = calcularValorFinal();
        this.status = StatusLocacao.CONCLUIDA;
    }

    public int getQuilometragemRodada() {
        return kmFinal == null ? 0 : kmFinal - kmInicial;
    }

    public Integer getIdLocacao() {
        return idLocacao;
    }

    public void setIdLocacao(Integer idLocacao) {
        this.idLocacao = idLocacao;
    }

    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDateTime dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    public void setDataDevolucaoPrevista(LocalDate dataDevolucaoPrevista) {
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
    }

    public int getKmInicial() {
        return kmInicial;
    }

    public void setKmInicial(int kmInicial) {
        this.kmInicial = kmInicial;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public BigDecimal getValorCaucao() {
        return valorCaucao;
    }

    public void setValorCaucao(BigDecimal valorCaucao) {
        this.valorCaucao = valorCaucao;
    }

    public StatusLocacao getStatus() {
        return status;
    }

    public void setStatus(StatusLocacao status) {
        this.status = status;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public LocalDateTime getDataHoraDevolucaoEfetiva() {
        return dataHoraDevolucaoEfetiva;
    }

    public void setDataHoraDevolucaoEfetiva(LocalDateTime dataHoraDevolucaoEfetiva) {
        this.dataHoraDevolucaoEfetiva = dataHoraDevolucaoEfetiva;
    }

    public Integer getKmFinal() {
        return kmFinal;
    }

    public void setKmFinal(Integer kmFinal) {
        this.kmFinal = kmFinal;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public String getVistoria() {
        return vistoria;
    }

    public void setVistoria(String vistoria) {
        this.vistoria = vistoria;
    }

    public BigDecimal getCustoAvaria() {
        return custoAvaria;
    }

    public void setCustoAvaria(BigDecimal custoAvaria) {
        this.custoAvaria = custoAvaria;
    }

    public Usuario getAtendente() {
        return atendente;
    }

    public void setAtendente(Usuario atendente) {
        this.atendente = atendente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locacao locacao = (Locacao) o;
        return Objects.equals(idLocacao, locacao.idLocacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLocacao);
    }
}
