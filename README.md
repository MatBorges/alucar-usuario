# Alucar — Módulo de Cadastro de Usuários

Implementação do RF04 (Manter Usuários) do Sistema de Gestão de Locadora de Veículos.

## Arquitetura em camadas

```
view/          TelaCadastroUsuario, UsuarioTableModel     (Swing — só apresentação)
controller/    ControladoraUsuario, ValidacaoException    (regras de negócio)
dao/           UsuarioDAO, ConexaoBD, DAOException        (persistência JDBC)
model/         Usuario, Atendente, Gerente, Mecanico,     (domínio)
               TipoUsuario
util/          SenhaUtil                                  (hash da senha)
```

A tela nunca acessa o banco diretamente: ela chama a controladora, que valida e delega ao DAO.
Essa separação é o padrão GRASP Controller aplicado ao caso de uso.

## Criar o banco

O projeto usa o container `lbd_mysql` já existente (MySQL publicado na porta 3434
do host, phpMyAdmin em http://localhost:4343). O banco `alucar` é criado ao lado
do `lbd_db`, sem interferir nele.

Pelo terminal do WSL:

```bash
docker exec -i lbd_mysql mysql -uroot -p123 < src/main/resources/schema.sql
```

Ou pelo phpMyAdmin: acesse http://localhost:4343, entre com `root` / `123`,
aba Importar, e envie o arquivo `src/main/resources/schema.sql`.

Para conferir:

```bash
docker exec -it lbd_mysql mysql -uroot -p123 -e "SELECT * FROM alucar.usuario;"
```

## Rodar a aplicação

```bash
mvn clean package
java -jar target/alucar-usuario-1.0.0.jar
```

### Se a aplicação roda no Windows e o banco no WSL

O WSL2 encaminha as portas publicadas para o Windows, então `localhost:3306` normalmente
funciona sem ajuste nenhum. Se der "Communications link failure", pegue o IP do WSL:

```bash
hostname -I        # dentro do WSL, ex: 172.24.112.35
```

E rode a aplicação apontando para ele, sem recompilar:

```powershell
$env:ALUCAR_DB_HOST="172.24.112.35"
java -jar target\alucar-usuario-1.0.0.jar
```

Esse IP muda a cada reinicialização do WSL — é por isso que o host é configurável
por variável de ambiente em vez de ficar fixo no código.

### Variáveis de ambiente aceitas

| Variável | Padrão |
|---|---|
| `ALUCAR_DB_HOST` | localhost |
| `ALUCAR_DB_PORT` | 3434 |
| `ALUCAR_DB_NAME` | alucar |
| `ALUCAR_DB_USER` | root |
| `ALUCAR_DB_PASSWORD` | 123 |

## Acesso inicial

O script cria um usuário gerente para o primeiro acesso:

- login: `admin`
- senha: `alucar123`

## Mapeamento objeto-relacional

A herança `Usuario → Atendente / Gerente / Mecanico` é mapeada por **tabela única**,
usando `tipo_usuario` como discriminador:

```
Usuario(matricula<<PK>>, nome, login, senha, tipoUsuario)
```

O método `UsuarioDAO.montarUsuario()` lê o discriminador e instancia a subclasse
correta via `Usuario.criar()`. É esse ponto que materializa o mapeamento.

## Observações

- A senha é gravada como hash SHA-256, nunca em texto puro.
- Todas as consultas usam `PreparedStatement` (previne SQL injection).
- Deixar a senha em branco na edição mantém a senha atual.
- A URL de conexão usa `sslMode=DISABLED`, `allowPublicKeyRetrieval=true` e
  `connectionTimeZone` — as três são necessárias com MySQL 8 em container.
