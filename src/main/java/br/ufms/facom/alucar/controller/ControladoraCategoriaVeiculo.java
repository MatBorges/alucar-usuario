package br.ufms.facom.alucar.controller;

import br.ufms.facom.alucar.dao.CategoriaVeiculoDAO;
import br.ufms.facom.alucar.dao.DAOException;
import br.ufms.facom.alucar.model.CategoriaVeiculo;
import br.ufms.facom.alucar.util.MoedaUtil;

import java.math.BigDecimal;
import java.util.List;


public class ControladoraCategoriaVeiculo {

    private static final BigDecimal VALOR_MAXIMO_DIARIA = new BigDecimal("99999.99");

    private final CategoriaVeiculoDAO categoriaDAO;

    public ControladoraCategoriaVeiculo() {
        this.categoriaDAO = new CategoriaVeiculoDAO();
    }

    public void cadastrarCategoria(String nome, String descricao, String valorBaseDiaria)
            throws ValidacaoException, DAOException {

        String nomeLimpo = limparNome(nome);
        BigDecimal valor = converterValor(valorBaseDiaria);
        validarDescricao(descricao);

        // O nome e chave primaria: uma categoria inativa continua ocupando o
        // valor, por isso a mensagem distingue os dois casos.
        CategoriaVeiculo existente = categoriaDAO.buscarPorNome(nomeLimpo);
        if (existente != null && !existente.isAtivo()) {
            throw new ValidacaoException("Ja existe uma categoria INATIVA chamada '"
                    + nomeLimpo + "'.\n\n"
                    + "Marque 'Mostrar inativas', selecione-a na lista e use Reativar.");
        }
        if (existente != null) {
            throw new ValidacaoException("Ja existe uma categoria cadastrada com o nome '"
                    + nomeLimpo + "'.");
        }

        categoriaDAO.inserir(new CategoriaVeiculo(nomeLimpo, trim(descricao), valor));
    }


    public void alterarCategoria(String nome, String descricao, String valorBaseDiaria)
            throws ValidacaoException, DAOException {

        String nomeLimpo = limparNome(nome);
        BigDecimal valor = converterValor(valorBaseDiaria);
        validarDescricao(descricao);

        CategoriaVeiculo existente = categoriaDAO.buscarPorNome(nomeLimpo);
        if (existente == null) {
            throw new ValidacaoException("Nenhuma categoria encontrada com o nome '"
                    + nomeLimpo + "'.");
        }

        categoriaDAO.atualizar(new CategoriaVeiculo(nomeLimpo, trim(descricao), valor));
    }


    public void inativarCategoria(String nome) throws ValidacaoException, DAOException {
        CategoriaVeiculo categoria = obrigarExistir(nome);

        if (!categoria.isAtivo()) {
            throw new ValidacaoException("Esta categoria ja esta inativa.");
        }
        categoriaDAO.inativar(categoria.getNome());
    }

    public void reativarCategoria(String nome) throws ValidacaoException, DAOException {
        CategoriaVeiculo categoria = obrigarExistir(nome);

        if (categoria.isAtivo()) {
            throw new ValidacaoException("Esta categoria ja esta ativa.");
        }
        categoriaDAO.reativar(categoria.getNome());
    }

    public List<CategoriaVeiculo> listarCategorias(boolean incluirInativas) throws DAOException {
        return categoriaDAO.listar(incluirInativas);
    }

    public CategoriaVeiculo buscarCategoria(String nome) throws DAOException {
        return categoriaDAO.buscarPorNome(nome == null ? "" : nome.trim());
    }


    public int contarVeiculosNaCategoria(String nome) throws DAOException {
        return categoriaDAO.contarVeiculosNaCategoria(nome);
    }

    private CategoriaVeiculo obrigarExistir(String nome) throws ValidacaoException, DAOException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Selecione uma categoria na lista.");
        }
        CategoriaVeiculo categoria = categoriaDAO.buscarPorNome(nome.trim());
        if (categoria == null) {
            throw new ValidacaoException("Nenhuma categoria encontrada com o nome '"
                    + nome.trim() + "'.");
        }
        return categoria;
    }

    private String limparNome(String nome) throws ValidacaoException {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Informe o nome da categoria.");
        }
        String limpo = nome.trim();
        if (limpo.length() < 3) {
            throw new ValidacaoException("O nome da categoria deve ter ao menos 3 caracteres.");
        }
        if (limpo.length() > 50) {
            throw new ValidacaoException("O nome da categoria deve ter no maximo 50 caracteres.");
        }
        return limpo;
    }

    private BigDecimal converterValor(String texto) throws ValidacaoException {
        BigDecimal valor;
        try {
            valor = MoedaUtil.converter(texto);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("Informe o valor base da diaria.\n"
                    + "Use o formato 240,00.");
        }

        if (valor.signum() <= 0) {
            throw new ValidacaoException("O valor base da diaria deve ser maior que zero.");
        }
        if (valor.compareTo(VALOR_MAXIMO_DIARIA) > 0) {
            throw new ValidacaoException("O valor base da diaria parece alto demais.\n"
                    + "Limite aceito: " + MoedaUtil.formatar(VALOR_MAXIMO_DIARIA) + ".");
        }
        return valor;
    }

    private void validarDescricao(String descricao) throws ValidacaoException {
        if (descricao != null && descricao.trim().length() > 200) {
            throw new ValidacaoException("A descricao deve ter no maximo 200 caracteres.");
        }
    }

    private String trim(String valor) {
        return valor == null ? null : valor.trim();
    }
}
