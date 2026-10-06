package br.ufms.facom.alucar.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * GRASP Information Expert = As regras que dependem apenas dos dados do proprio estão aqui na casse
 */
public class Veiculo {


    public static final int KM_LIMITE_DEPRECIACAO = 100_000;


    public static final int ANOS_LIMITE_DEPRECIACAO = 5;

    private String placa;
    private String renavam;
    private String modelo;
    private String marca;
    private int anoFabricacao;
    private int kmAtual;
    private BigDecimal valorDiaria;
    private StatusVeiculo status = StatusVeiculo.DISPONIVEL;
    private CategoriaVeiculo categoria;

    public Veiculo() {
    }

    public Veiculo(String placa, String renavam, String modelo, String marca,
                   int anoFabricacao, int kmAtual, BigDecimal valorDiaria,
                   StatusVeiculo status, CategoriaVeiculo categoria) {
        this.placa = placa;
        this.renavam = renavam;
        this.modelo = modelo;
        this.marca = marca;
        this.anoFabricacao = anoFabricacao;
        this.kmAtual = kmAtual;
        this.valorDiaria = valorDiaria;
        this.status = status;
        this.categoria = categoria;
    }

    /** RN02 - apenas veiculos disponiveis podem ser alugados ou reservados. */
    public boolean estaDisponivel() {
        return status == StatusVeiculo.DISPONIVEL;
    }

    public void alterarStatus(StatusVeiculo novoStatus) {
        this.status = novoStatus;
    }

    /** Idade do veiculo em anos, em relacao ao ano corrente. */
    public int getIdadeEmAnos() {
        return LocalDate.now().getYear() - anoFabricacao;
    }

    /**
     * RN04 - veiculos com mais de 100.000 km rodados ou mais de 5 anos de
     * fabricacao atingiram o limite de depreciacao e devem sair de servico.
     */
    public boolean atingiuLimiteDepreciacao() {
        return kmAtual > KM_LIMITE_DEPRECIACAO
                || getIdadeEmAnos() > ANOS_LIMITE_DEPRECIACAO;
    }

    /** Explica qual dos dois limites da RN04 foi ultrapassado. */
    public String descreverMotivoDaDepreciacao() {
        boolean porKm = kmAtual > KM_LIMITE_DEPRECIACAO;
        boolean porIdade = getIdadeEmAnos() > ANOS_LIMITE_DEPRECIACAO;

        if (porKm && porIdade) {
            return "quilometragem acima de " + KM_LIMITE_DEPRECIACAO + " km e "
                    + getIdadeEmAnos() + " anos de fabricacao";
        }
        if (porKm) {
            return "quilometragem de " + kmAtual + " km, acima do limite de "
                    + KM_LIMITE_DEPRECIACAO + " km";
        }
        if (porIdade) {
            return getIdadeEmAnos() + " anos de fabricacao, acima do limite de "
                    + ANOS_LIMITE_DEPRECIACAO + " anos";
        }
        return "";
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getRenavam() {
        return renavam;
    }

    public void setRenavam(String renavam) {
        this.renavam = renavam;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getAnoFabricacao() {
        return anoFabricacao;
    }

    public void setAnoFabricacao(int anoFabricacao) {
        this.anoFabricacao = anoFabricacao;
    }

    public int getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(int kmAtual) {
        this.kmAtual = kmAtual;
    }

    public BigDecimal getValorDiaria() {
        return valorDiaria;
    }

    public void setValorDiaria(BigDecimal valorDiaria) {
        this.valorDiaria = valorDiaria;
    }

    public StatusVeiculo getStatus() {
        return status;
    }

    public void setStatus(StatusVeiculo status) {
        this.status = status;
    }

    public CategoriaVeiculo getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaVeiculo categoria) {
        this.categoria = categoria;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Veiculo outro)) {
            return false;
        }
        return Objects.equals(placa, outro.placa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placa);
    }

    @Override
    public String toString() {
        return placa + " - " + marca + " " + modelo + " (" + anoFabricacao + ")";
    }
}
