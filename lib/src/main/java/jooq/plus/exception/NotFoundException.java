package jooq.plus.exception;

/**
 * not found exception.
 *
 * @author spencercjh
 */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String msg) {
    super(msg);
  }
}
