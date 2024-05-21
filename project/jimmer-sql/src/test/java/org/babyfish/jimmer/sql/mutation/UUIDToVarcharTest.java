package org.babyfish.jimmer.sql.mutation;

import org.babyfish.jimmer.sql.ast.mutation.AffectedTable;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.common.AbstractMutationTest;
import org.babyfish.jimmer.sql.model.uuid.UUIDToVarcharModel;
import org.babyfish.jimmer.sql.model.uuid.UUIDToVarcharModelDraft;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class UUIDToVarcharTest extends AbstractMutationTest {

  @Test
  public void testUUIDToVarcharConversion() {
    UUID newId = UUID.fromString("56506a3c-801b-4f7d-a41d-e889cdc3d67d");
    executeAndExpectResult(
        getSqlClient().getEntities().saveCommand(
            UUIDToVarcharModelDraft.$.produce(model -> {
              model.setId(newId);
            })
        ).configure(cfg -> cfg.setMode(SaveMode.INSERT_ONLY)),
        ctx -> {
          ctx.statement(it -> {
            it.sql(
                "insert into uuid_to_varchar_id_model(ID) values(?)"
            );
            it.variables(
                // uuid should be replaced to string
                newId.toString()
            );
          });
          ctx.totalRowCount(1);
          ctx.rowCount(AffectedTable.of(UUIDToVarcharModel.class), 1);
        }
    );
  }
}
