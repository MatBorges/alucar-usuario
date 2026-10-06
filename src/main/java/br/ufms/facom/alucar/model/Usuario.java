package br.ufms.facom.alucar.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


public abstract class Usuario {

    private String matricula;
    private String cpf;
    private String nome;
    private String login;
    private String senha;


    private LocalDate dataUltimaTrocaSenha = LocalDate.now();


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


    public abstract TipoUsuario getTipoUsuario();


    public static Usuario criar(TipoUsuario tipo, String matricula, String cpf,
                                String nome, String login, String senha) {
        Objects.requireNonNull(tipo, "Tipo de usuario nao informado");
        return switch (tipo) {
            case GERENTE -> new Gerente(matricula, cpf, nome, login, senha);
            case ATENDENTE -> new Atendente(matricula, cpf, nome, login, senha);
            case MECANICO -> new Mecanico(matricula, cpf, nome, login, senha);
        };
    }


    public boolean autenticar(String loginInformado, String senhaInformada) {
        return ativo
                && this.login != null
                && this.login.equals(loginInformado)
                && this.senha != null
                && this.senha.equals(senhaInformada);
    }


    public boolean senhaExpirada(int prazoEmDias) {
        if (prazoEmDias <= 0 || dataUltimaTrocaSenha == null) {
            return false;
        }
        return LocalDate.now().isAfter(dataUltimaTrocaSenha.plusDays(prazoEmDias));
    }


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
