package org.babyfish.jimmer.sql.model.uuid;

import org.babyfish.jimmer.sql.Column;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Table;

import java.util.UUID;

@Entity
@Table(name = "uuid_to_varchar_id_model")
public interface UUIDToVarcharModel {
  @Id
  @Column(sqlType = "text")
  UUID id();
}
