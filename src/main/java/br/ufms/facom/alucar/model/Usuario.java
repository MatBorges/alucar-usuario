package br.ufms.facom.alucar.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Classe base da hierarquia de usuarios do sistema (RF04).
 *
 * A heranca Usuario -> Atendente / Gerente / Mecanico e mapeada para uma
 * unica tabela relacional (usuario), usando a coluna tipo_usuario como
 * discriminador. Por isso a classe e abstrata: nao existe "usuario generico",
 * todo usuario persistido tem um tipo concreto.
 */
public abstract class Usuario {

    private String matricula;
    private String cpf;
    private String nome;
    private String login;
    private String senha;

    /**
     * Data da ultima troca de senha. Base para o RNF03: o sistema compara
     * esta data com o prazo de expiracao configurado para decidir se a
     * redefinicao e obrigatoria.
     */
    private LocalDate dataUltimaTrocaSenha = LocalDate.now();

    /**
     * Exclusao logica: um usuario inativo deixa de aparecer nas listagens e
     * nao pode mais operar o sistema, mas continua existindo no banco para
     * preservar o historico das locacoes que ele realizou.
     */
    private boolean ativo = true;

    protected Usuario() {
    }

    protected Usuario(String matricula, String cpf, String nome, String login, String senha) {
        this.matricula = matricula;
        this.cpf = cpf;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    /**
     * Discriminador da heranca. Cada subclasse devolve o seu proprio tipo,
     * e e esse valor que vai para a coluna tipo_usuario.
     */
    public abstract TipoUsuario getTipoUsuario();

    /**
     * Fabrica usada pelo DAO ao ler uma linha da tabela usuario: converte o
     * valor do discriminador na subclasse correspondente.
     */
    public static Usuario criar(TipoUsuario tipo, String matricula, String cpf,
                                String nome, String login, String senha) {
        Objects.requireNonNull(tipo, "Tipo de usuario nao informado");
        return switch (tipo) {
            case GERENTE -> new Gerente(matricula, cpf, nome, login, senha);
            case ATENDENTE -> new Atendente(matricula, cpf, nome, login, senha);
            case MECANICO -> new Mecanico(matricula, cpf, nome, login, senha);
        };
    }

    /** RF01 - autenticacao por login e senha. Usuario inativo nao autentica. */
    public boolean autenticar(String loginInformado, String senhaInformada) {
        return ativo
                && this.login != null
                && this.login.equals(loginInformado)
                && this.senha != null
                && this.senha.equals(senhaInformada);
    }

    /**
     * RNF03 - indica se a senha ultrapassou o prazo de validade configurado.
     * Um prazo menor ou igual a zero desliga a expiracao.
     */
    public boolean senhaExpirada(int prazoEmDias) {
        if (prazoEmDias <= 0 || dataUltimaTrocaSenha == null) {
            return false;
        }
        return LocalDate.now().isAfter(dataUltimaTrocaSenha.plusDays(prazoEmDias));
    }

    /**
     * Quantos dias faltam para a senha expirar. Devolve um numero negativo
     * quando ela ja esta vencida.
     */
    public long diasAteExpirarSenha(int prazoEmDias) {
        if (prazoEmDias <= 0 || dataUltimaTrocaSenha == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(),
                dataUltimaTrocaSenha.plusDays(prazoEmDias));
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDate getDataUltimaTrocaSenha() {
        return dataUltimaTrocaSenha;
    }

    public void setDataUltimaTrocaSenha(LocalDate dataUltimaTrocaSenha) {
        this.dataUltimaTrocaSenha = dataUltimaTrocaSenha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Usuario outro)) {
            return false;
        }
        return Objects.equals(matricula, outro.matricula);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matricula);
    }

    @Override
    public String toString() {
        return matricula + " - " + nome + " (" + getTipoUsuario() + ")";
    }
}
