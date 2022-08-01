package jooq.plus;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import jooq.plus.exception.CreateRecordException;
import jooq.plus.exception.DeleteRecordException;
import jooq.plus.exception.UpdateRecordException;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DAOImpl;

/**
 * Abstract CRUD repository implementation.
 *
 * @param <PojoT> JOOQ generated pojo type
 * @param <DaoT>  JOOQ generated dao type
 * @param <PkT>   primary key (id) type, {@link java.util.UUID}, {@link Long} or somthing else.
 * @author spencercjh
 */
@Blocking
@SuppressWarnings("rawtypes")
public abstract class AbstractCrudRepository<PojoT, DaoT extends DAOImpl, PkT>
    implements BaseCrudRepository<PojoT, PkT> {

  protected TypeToken<PkT> primaryKeyType = new TypeToken<PkT>(getClass()) {
  };
  protected TypeToken<PojoT> pojoType = new TypeToken<PojoT>(getClass()) {
  };
  protected DSLContext dslContext;
  protected DaoT dao;
  protected Table table;
  protected Set<Field> fields;
  protected TableField primaryKeyField;

  public AbstractCrudRepository() {
  }

  /**
   * Instantiates a new Abstract crud repository.
   *
   * @param dao the dao
   */
  public AbstractCrudRepository(@NotNull DaoT dao) {
    this.table = dao.getTable();
    this.fields = Sets.newHashSet(table.fields());
    this.dslContext = dao.ctx();
    this.dao = dao;

    if (Objects.isNull(table.getPrimaryKey())
        || ArrayUtils.isEmpty(table.getPrimaryKey().getFieldsArray())) {
      throw new IllegalStateException(
          String.format("can't get table: %s primary key", table.getName()));
    }

    if (!Objects.equals(dao.getType(), pojoType.getRawType())) {
      throw new IllegalArgumentException(String.format(
          "dao type: %s must be same pojo: %s", dao.getType(), pojoType.getRawType()));
    }

    primaryKeyField = table.getPrimaryKey().getFieldsArray()[0];
  }

  @Override
  public Table getTable() {
    return table;
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public @NotNull PojoT create(@NotNull PojoT toCreate) {
    if (Objects.equals(Long.class, primaryKeyType.getRawType())) {
      final Record newRecord = dslContext.newRecord(table);
      newRecord.from(toCreate);
      try {
        final Record record = dslContext.insertInto(table)
            .set(newRecord)
            .returningResult(primaryKeyField)
            .fetchOne();
        if (record == null) {
          throw new CreateRecordException("create revision record failed to get pk");
        }
        final Long primaryKey = ((Long) record.getValue(primaryKeyField));
        newRecord.set(primaryKeyField, primaryKey);
        return (PojoT) newRecord.into(toCreate.getClass());
      } catch (DataAccessException e) {
        throw new CreateRecordException(e);
      }
    }
    try {
      dao.insert(toCreate);
    } catch (DataAccessException e) {
      throw new CreateRecordException(e);
    }
    return toCreate;
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public @NotNull Optional<PojoT> retrieve(@NotNull PkT id) {
    return dao.fetchOptional(primaryKeyField, id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public @NotNull <U> Optional<PojoT> retrieveBy(@NotNull Field<U> field, U value) {
    if (!fields.contains(field)) {
      throw new IllegalArgumentException(
          String.format("field: %s not found in table: %s", field.getName(), table.getName()));
    }
    return dao.fetchOptional(field, value);
  }

  @Override
  public void update(@NotNull PojoT toUpdate) {
    update(toUpdate, false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void update(@NotNull PojoT toUpdate, boolean checkExist) {
    if (checkExist && !dao.exists(toUpdate)) {
      throw new UpdateRecordException(
          String.format("record: %s not found to update", table.getName()));
    }
    dao.update(toUpdate);
  }

  @Override
  public void delete(@NotNull PkT id) {
    delete(id, false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void delete(@NotNull PkT id, boolean checkExist) {
    if (checkExist && !dao.existsById(id)) {
      throw new DeleteRecordException(
          String.format("record: %s not found to delete", table.getName()));
    }
    dao.deleteById(id);
  }

  @Override
  public void delete(@NotNull Collection<PkT> ids) {
    delete(ids, false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void delete(@NotNull Collection<PkT> ids, boolean checkExist) {
    if (checkExist && !exists(Lists.newArrayList(primaryKeyField.in(ids)))) {
      throw new DeleteRecordException(
          String.format("%s records not found to delete", table.getName()));
    }
    dao.deleteById(ids);
  }

  @Override
  public int count(@NotNull Collection<Condition> conditions) {
    // NOTICE: check conditions are actually legal for the current table
    return dslContext.fetchCount(table, conditions);
  }

  @Override
  public boolean exists(@NotNull Collection<Condition> conditions) {
    // NOTICE: check conditions are actually legal for the current table
    return dslContext.fetchExists(table, conditions);
  }

  @SuppressWarnings("unchecked")
  @Override
  public @NotNull Collection<PojoT> list(@NotNull Collection<Condition> conditions) {
    // NOTICE: check conditions are actually legal for the current table
    return dslContext.selectFrom(table).where(conditions).fetchInto(dao.getType());
  }
}
