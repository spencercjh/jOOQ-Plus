package jooq.plus.exception;

/**
 * create record exception.
 *
 * @author spencercjh
 */
public class CreateRecordException extends RuntimeException {

  public CreateRecordException(String msg) {
    super(msg);
  }

  public CreateRecordException(Throwable cause) {
    super(cause);
  }
}
