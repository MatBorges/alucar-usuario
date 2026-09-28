package br.ufms.facom.alucar.model;

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
    private String nome;
    private String login;
    private String senha;

    /**
     * Exclusao logica: um usuario inativo deixa de aparecer nas listagens e
     * nao pode mais operar o sistema, mas continua existindo no banco para
     * preservar o historico das locacoes que ele realizou.
     */
    private boolean ativo = true;

    protected Usuario() {
    }

    protected Usuario(String matricula, String nome, String login, String senha) {
        this.matricula = matricula;
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
    public static Usuario criar(TipoUsuario tipo, String matricula, String nome,
                                String login, String senha) {
        Objects.requireNonNull(tipo, "Tipo de usuario nao informado");
        return switch (tipo) {
            case GERENTE -> new Gerente(matricula, nome, login, senha);
            case ATENDENTE -> new Atendente(matricula, nome, login, senha);
            case MECANICO -> new Mecanico(matricula, nome, login, senha);
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

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
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
