package org.leveloneproject.central.kms.persistance

import java.sql.SQLException

trait DatabaseHelper {
  def isPrimaryKeyViolation(ex: SQLException): Boolean = ex.getSQLState == "23505"
}
