package jooq.plus;

import java.util.Collection;
import java.util.Optional;
import jooq.plus.exception.CreateRecordException;
import jooq.plus.exception.DeleteRecordException;
import jooq.plus.exception.UpdateRecordException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;

/**
 * Base CRUD repository like the one in JPA like
 * <a href="https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.core-concepts">spring data</a> .
 * <p>All methods should be pure.</p>
 *
 * @param <PojoT> JOOQ generated pojo type
 * @param <PkT>   primary key (id) type, {@link java.util.UUID} or {@link Long}
 * @author spencercjh
 */
public interface BaseCrudRepository<PojoT, PkT> {
  /**
   * Gets current repository's table.
   *
   * @return the table
   */
  @SuppressWarnings("rawtypes")
  Table getTable();

  /**
   * insert.
   *
   * @param toCreate pojo to insert
   * @return new object if the {@link PkT} is {@link Long} otherwise return the param1
   * @throws CreateRecordException if insert failed
   */
  @Contract(value = "_ -> _", pure = true)
  @NotNull
  PojoT create(@NotNull PojoT toCreate) throws CreateRecordException;

  /**
   * select by id.
   *
   * @param id pk id
   * @return optional with the result or empty optional if not found
   */
  @Contract(value = "_ -> new", pure = true)
  @NotNull
  Optional<PojoT> retrieve(@NotNull PkT id);

  /**
   * select by specified field and its value.
   *
   * @param <U>   the specified field type
   * @param field the field
   * @param value the value
   * @return optional with the result or empty optional if not found
   * @throws IllegalArgumentException if the field is not in the pojo's table
   */
  @Contract(value = "_ , _ -> new", pure = true)
  @NotNull <U> Optional<PojoT> retrieveBy(@NotNull Field<U> field, @Nullable U value);

  /**
   * update by id.
   * <p><b>NOTICE: there will be no any side-effect(like thrown exception)
   * when update failed with 0 affected row.</b></p>
   * <p><b>If you want to Fail-fast, please use {@link #update(PojoT, boolean)} with true.</b></p>
   *
   * @param toUpdate pojo to update
   */
  @Contract(pure = true)
  void update(@NotNull PojoT toUpdate);

  /**
   * update by id after checking the entity exists.
   *
   * @param toUpdate   pojo to update
   * @param checkExist whether it needs to be checked
   * @throws UpdateRecordException thrown when precondition check failed
   */
  @Contract(pure = true)
  void update(@NotNull PojoT toUpdate, boolean checkExist) throws UpdateRecordException;

  /**
   * delete by id.
   * <p><b>NOTICE: there will be no any side-effect(like thrown exception)
   * when delete failed with 0 affected row.</b></p>
   * <p><b>If you want to Fail-fast, please use {@link #delete(PkT, boolean)} with true.</b></p>
   *
   * @param id id to delete
   */
  @Contract(pure = true)
  void delete(@NotNull PkT id);

  /**
   * delete by id after checking the entity exists.
   *
   * @param id         id to delete
   * @param checkExist whether it needs to be checked
   * @throws DeleteRecordException thrown when precondition check failed
   */
  @Contract(pure = true)
  void delete(@NotNull PkT id, boolean checkExist) throws DeleteRecordException;

  /**
   * delete by ids.
   * <p><b>NOTICE: there will be no any side-effect(like thrown exception)
   * when delete failed with 0 affected row.</b></p>
   * <p><b>If you want to Fail-fast,
   * please use {@link #delete(Collection, boolean)} with true.</b></p>
   *
   * @param ids ids to delete
   */
  @Contract(pure = true)
  void delete(@NotNull Collection<PkT> ids);

  /**
   * delete by ids after checking the entity exists.
   *
   * @param ids        ids to delete
   * @param checkExist whether it needs to be checked
   * @throws DeleteRecordException thrown when precondition check failed
   */
  @Contract(pure = true)
  void delete(@NotNull Collection<PkT> ids, boolean checkExist) throws DeleteRecordException;

  /**
   * count by conditions.
   *
   * @param conditions the conditions
   * @return result
   */
  @Contract(value = "_ -> _", pure = true)
  int count(@NotNull Collection<Condition> conditions);

  /**
   * SELECT EXISTS(SELECT * FROM table WHERE condition).
   *
   * @param conditions the conditions
   * @return result
   */
  @Contract(value = "_ -> _", pure = true)
  boolean exists(@NotNull Collection<Condition> conditions);

  /**
   * select by conditions.
   *
   * @param conditions the conditions, allow empty
   * @return result
   */
  @Contract(value = "_  -> new", pure = true)
  @NotNull
  Collection<PojoT> list(@NotNull Collection<Condition> conditions);
}
