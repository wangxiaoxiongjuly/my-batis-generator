package config.content;

/**
 * @author wenxuan.wang
 */
public class Function {
    private Function() {
    }

    public static final String INSERT_BATCH_SELECTIVE = "insertBatchSelective";

    public static final String UPDATE_BATCH_BY_PRIMARY_KEY_SELECTIVE = "updateBatchByPrimaryKeySelective";

    public static final String UPDATE_BATCH_BY_EXAMPLE_SELECTIVE = "updateBatchByExampleSelective";

    public static final String ORACLE_DIALECT_PREFIX = "OracleDialectPrefix";

    public static final String ORACLE_DIALECT_SUFFIX = "OracleDialectSuffix";
}
