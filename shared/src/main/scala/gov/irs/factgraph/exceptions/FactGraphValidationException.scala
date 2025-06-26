package gov.irs.factgraph.exceptions

case class FactGraphValidationException(message: String) extends Exception(message)