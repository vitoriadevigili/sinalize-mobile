package br.com.sinalize.api;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testa a migration {@code V3__nomes_unicos.sql} partindo de um banco que já tem nomes repetidos
 * (situação de quem usou a API antes desta versão).
 *
 * <p>Roda sem o Spring, num schema próprio ({@code it_v3}), para não interferir no schema
 * {@code it} usado pelos demais testes de integração. Pré-requisito: {@code docker compose up -d}.</p>
 */
class MigrationNomesUnicosIT {

    private static final String SCHEMA = "it_v3";

    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbc;

    @BeforeEach
    void prepararBancoNaVersao2() {
        dataSource = new DriverManagerDataSource(
                Objects.requireNonNullElse(System.getenv("DB_URL"), "jdbc:postgresql://localhost:5433/sinalize"),
                Objects.requireNonNullElse(System.getenv("DB_USER"), "sinalize"),
                Objects.requireNonNullElse(System.getenv("DB_PASSWORD"), "sinalize"));
        jdbc = new JdbcTemplate(dataSource);

        Flyway versao2 = flyway("2");
        versao2.clean();
        versao2.migrate();
    }

    @AfterEach
    void removerSchema() {
        jdbc.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
    }

    @Test
    void unePalavrasECategoriasRepetidasECriaOsIndicesUnicos() {
        // Duplicatas criadas antes da V3: " uva " (mais antiga) e "UVA", e a categoria "cores " repetida.
        long frutas = inserir("INSERT INTO " + SCHEMA + ".categorias (nome, descricao, criado_por) "
                + "VALUES ('Frutas', 'd', 1) RETURNING id");
        long coresRepetida = inserir("INSERT INTO " + SCHEMA + ".categorias (nome, descricao, criado_por) "
                + "VALUES ('cores ', 'd', 1) RETURNING id");
        long cores = idDaCategoria("Cores");
        long alimentos = idDaCategoria("Alimentos");

        long uvaAntiga = inserir("INSERT INTO " + SCHEMA + ".palavras (palavra, criado_por) "
                + "VALUES (' uva ', 1) RETURNING id");
        long uvaNova = inserir("INSERT INTO " + SCHEMA + ".palavras (palavra, criado_por) "
                + "VALUES ('UVA', 1) RETURNING id");
        vincular(uvaAntiga, alimentos);
        vincular(uvaNova, frutas);

        long lilas = inserir("INSERT INTO " + SCHEMA + ".palavras (palavra, criado_por) "
                + "VALUES ('Lilás', 1) RETURNING id");
        vincular(lilas, coresRepetida);

        // a palavra repetida já foi usada em um quiz (FK RESTRICT)
        long quiz = inserir("INSERT INTO " + SCHEMA + ".quizzes (id_usuario) VALUES (1) RETURNING id");
        jdbc.update("INSERT INTO " + SCHEMA + ".quiz_palavras (id_quiz, id_palavra) VALUES (?, ?)", quiz, uvaNova);

        flyway(null).migrate();

        // fica só a "uva" mais antiga, sem espaços, com as categorias das duas e a resposta do quiz
        assertThat(jdbc.queryForList("SELECT id FROM " + SCHEMA + ".palavras WHERE lower(trim(palavra)) = 'uva'",
                Long.class)).containsExactly(uvaAntiga);
        assertThat(jdbc.queryForObject("SELECT palavra FROM " + SCHEMA + ".palavras WHERE id = ?",
                String.class, uvaAntiga)).isEqualTo("uva");
        assertThat(categoriasDa(uvaAntiga)).containsExactlyInAnyOrder(alimentos, frutas);
        assertThat(jdbc.queryForObject("SELECT id_palavra FROM " + SCHEMA + ".quiz_palavras WHERE id_quiz = ?",
                Long.class, quiz)).isEqualTo(uvaAntiga);

        // a categoria repetida some e a palavra dela passa para a "Cores" original
        assertThat(jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA + ".categorias WHERE id = ?",
                Integer.class, coresRepetida)).isZero();
        assertThat(categoriasDa(lilas)).containsExactly(cores);

        // os índices únicos passam a barrar novas repetições
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO " + SCHEMA + ".palavras (palavra, criado_por) VALUES (' Uva', 1)"))
                .isInstanceOf(DuplicateKeyException.class);
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO " + SCHEMA + ".categorias (nome, descricao, criado_por) VALUES ('FRUTAS', 'd', 1)"))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void semRepeticoesMantemOsDadosIniciais() {
        Integer palavrasAntes = jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA + ".palavras", Integer.class);
        Integer vinculosAntes = jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA + ".palavras_categorias",
                Integer.class);

        flyway(null).migrate();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA + ".palavras", Integer.class))
                .isEqualTo(palavrasAntes).isEqualTo(44);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA + ".palavras_categorias", Integer.class))
                .isEqualTo(vinculosAntes);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM " + SCHEMA + ".categorias", Integer.class))
                .isEqualTo(6);
    }

    /** Flyway no schema de teste; {@code alvo} limita a versão aplicada (null = todas). */
    private Flyway flyway(String alvo) {
        var configuracao = Flyway.configure()
                .dataSource(dataSource)
                .schemas(SCHEMA)
                .defaultSchema(SCHEMA)
                .createSchemas(true)
                .cleanDisabled(false)
                .locations("classpath:db/migration");
        if (alvo != null) {
            configuracao.target(alvo);
        }
        return configuracao.load();
    }

    private long inserir(String sql) {
        Long id = jdbc.queryForObject(sql, Long.class);
        assertThat(id).isNotNull();
        return id;
    }

    private void vincular(long palavraId, long categoriaId) {
        jdbc.update("INSERT INTO " + SCHEMA + ".palavras_categorias (palavra_id, categoria_id) VALUES (?, ?)",
                palavraId, categoriaId);
    }

    private long idDaCategoria(String nome) {
        Long id = jdbc.queryForObject("SELECT id FROM " + SCHEMA + ".categorias WHERE nome = ?", Long.class, nome);
        assertThat(id).isNotNull();
        return id;
    }

    private List<Long> categoriasDa(long palavraId) {
        return jdbc.queryForList("SELECT categoria_id FROM " + SCHEMA + ".palavras_categorias WHERE palavra_id = ?",
                Long.class, palavraId);
    }
}
