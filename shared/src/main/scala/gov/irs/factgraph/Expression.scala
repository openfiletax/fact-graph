package gov.irs.factgraph

import gov.irs.factgraph.operators.*
import gov.irs.factgraph.monads.*
import gov.irs.factgraph.types.{CollectionItem, WritableType}
import gov.irs.factgraph.compnodes.Placeholder

enum Expression[A]:
  case Constant(a: Option[A])
  case Writable(klass: Class[A])
  case Dependency(path: Path)
  case Switch(cases: List[(Expression[Boolean], Expression[A])])
  case Extract[A, X](f: (source: Result[X]) => Result[A]) extends Expression[A]
  case Unary[A, X](
      x: Expression[X],
      op: UnaryOperator[A, X],
  ) extends Expression[A]
  case Binary[A, L, R](
      lhs: Expression[L],
      rhs: Expression[R],
      op: BinaryOperator[A, L, R],
  ) extends Expression[A]
  case Arity4[A, W, X, Y, Z](
      arg1: Expression[W],
      arg2: Expression[X],
      arg3: Expression[Y],
      arg4: Expression[Z],
      op: Arity4Operator[A, W, X, Y, Z],
  ) extends Expression[A]
  case Reduce(
      xs: List[Expression[A]],
      op: ReduceOperator[A],
  )
  // Returns the set of strings where the corresponding booleans are true.
  // If any string or boolean are incomplete, ConditionalList will return
  // Result.Incomplete
  case ConditionalList(options: List[(Expression[Boolean], Expression[String])]) extends Expression[List[String]]
  case Aggregate[A, X](
      x: Expression[X],
      op: AggregateOperator[A, X],
  ) extends Expression[A]
  case Collect[A, X](
      path: Path,
      x: Expression[X],
      op: CollectOperator[A, X],
  ) extends Expression[A]

  def isWritable: Boolean = this match
    case Writable(_) => true
    case _           => Placeholder.isWritablePlaceholder(this)

  def get(using Factual): MaybeVector[Result[A]] = this match
    case Constant(a)          => MaybeVector(Result(a))
    case Writable(klass)      => MaybeVector(getSavedResult(klass))
    case Dependency(path)     => dependency(path, dependencies.result)
    case Switch(cases)        => switch(cases, switches.result)
    case Extract(f)           => extract(f)
    case Unary(x, op)         => op(x.get)
    case Binary(lhs, rhs, op) => op(lhs.get, rhs.getThunk)
    case Arity4(arg1, arg2, arg3, arg4, op) =>
      op(arg1.get, arg2.get, arg3.get, arg4.get)
    case Reduce(xs, op)       => op(xs.head.get, xs.tail.map(_.getThunk))
    case ConditionalList(l)   => MaybeVector(conditionSet(l))
    case Aggregate(x, op)     => MaybeVector(op(x.getThunk))
    case Collect(path, x, op) => MaybeVector(collect(path, x, op))

  def getThunk(using Factual): MaybeVector[Thunk[Result[A]]] = this match
    case Constant(a)          => MaybeVector(Thunk(() => Result(a)))
    case Writable(klass)      => MaybeVector(Thunk(() => getSavedResult(klass)))
    case Dependency(path)     => dependency(path, dependencies.thunk)
    case Switch(cases)        => switch(cases, switches.thunk)
    case Extract(f)           => extractThunk(f)
    case Unary(x, op)         => op.thunk(x.getThunk)
    case Binary(lhs, rhs, op) => op.thunk(lhs.getThunk, rhs.getThunk)
    case Arity4(arg1, arg2, arg3, arg4, op) =>
      op.thunk(arg1.getThunk, arg2.getThunk, arg3.getThunk, arg4.getThunk)
    case Reduce(xs, op)       => op.thunk(xs.head.getThunk, xs.tail.map(_.getThunk))
    case ConditionalList(l)   => MaybeVector(Thunk(() => conditionSet(l)))
    case Aggregate(x, op)     => MaybeVector(op.thunk(x.getThunk))
    case Collect(path, x, op) => MaybeVector(Thunk(() => collect(path, x, op)))

  def explain(using fact: Factual): MaybeVector[Explanation] = this match
    case Constant(_) => MaybeVector(Explanation.Constant)
    case Writable(_) =>
      for result <- get yield Explanation.Writable(result.complete, fact.path)
    case Dependency(path)     => dependency(path, dependencies.explain)
    case Switch(cases)        => switchExplain(cases)
    case Extract(f)           => fact(PathItem.Parent)(0).get.explain
    case Unary(x, op)         => op.explain(x)
    case Binary(lhs, rhs, op) => op.explain(lhs, rhs)
    case Arity4(arg1, arg2, arg3, arg4, op) =>
      op.explain(arg1, arg2, arg3, arg4)
    case Reduce(xs, op)       => op.explain(xs)
    case ConditionalList(l)   => conditionExplain(l)
    case Aggregate(x, op)     => op.explain(x)
    case Collect(path, x, op) => op.explain(path, x)

  def set(value: WritableType)(using fact: Factual): Unit = this match
    case Writable(_) =>
      fact match
        case fact: Fact => fact.graph.persister.setFact(fact, value)
    case _ =>

  def delete()(using fact: Factual): Unit = this match
    case Writable(_) =>
      fact match
        case fact: Fact => fact.graph.persister.deleteFact(fact)
    case _ =>

  private def getSavedResult(klass: Class[A])(using fact: Factual): Result[A] =
    fact match
      case fact: Fact => fact.graph.persister.getSavedResult(fact.path, klass)
      case _          => Result.Incomplete

  private def dependency[X](
      path: Path,
      f: (Result[Factual], Path, Path) => MaybeVector[X],
  )(using
      fact: Factual,
  ): MaybeVector[X] =
    for {
      result <- fact(path)
      vect <- f(result, fact.path, path)
    } yield vect

  private object dependencies:
    def result(
        result: Result[Factual],
        _1: Path,
        _2: Path,
    ): MaybeVector[Result[A]] = result match
      case Result(fact, complete) =>
        val results = fact.get.asInstanceOf[MaybeVector[Result[A]]]
        if (!complete) results.map(_.asPlaceholder) else results
      case _ => MaybeVector(Result.Incomplete)

    def thunk(
        result: Result[Factual],
        _1: Path,
        _2: Path,
    ): MaybeVector[Thunk[Result[A]]] =
      result match
        case Result(fact, complete) =>
          val thunks = fact.size match
            case Factual.Size.Single =>
              MaybeVector(Thunk(() => fact.get(0).asInstanceOf[Result[A]]))
            case Factual.Size.Multiple =>
              fact.getThunk.asInstanceOf[MaybeVector[Thunk[Result[A]]]]

          if (!complete)
            for {
              thunk <- thunks
            } yield for {
              result <- thunk
            } yield result.asPlaceholder
          else thunks
        case _ => MaybeVector(Thunk(() => Result.Incomplete))

    def explain(
        result: Result[Factual],
        source: Path,
        target: Path,
    ): MaybeVector[Explanation] =
      result match
        case Result(fact, complete) =>
          for {
            explanation <- fact.explain
          } yield Explanation.Dependency(
            complete,
            source,
            target,
            List(List(explanation)),
          )
        case _ =>
          MaybeVector(Explanation.Dependency(false, source, target, List()))

  private def switch[X](
      cases: List[(Expression[Boolean], Expression[A])],
      f: List[(Thunk[Result[Boolean]], Thunk[Result[A]])] => X,
  )(using Factual): MaybeVector[X] =
    val thunks = cases.map((bool, a) => (bool.getThunk, a.getThunk))
    MaybeVector.vectorizeListTuple2(f, thunks)

  private def switchExplain(
      cases: List[(Expression[Boolean], Expression[A])],
  )(using Factual): MaybeVector[Explanation] =
    val caseVectors = cases.map((bool, a) =>
      (
        bool.getThunk,
        bool.explain,
        a.explain,
      ), // Potential optimization: thunk the explanations
    )

    MaybeVector.vectorizeListTuple3(switches.explain, caseVectors)

  private object switches:
    def result(
        cases: List[(Thunk[Result[Boolean]], Thunk[Result[A]])],
    ): Result[A] =
      resultRecurse(cases, true)

    @annotation.tailrec
    private def resultRecurse(
        cases: List[(Thunk[Result[Boolean]], Thunk[Result[A]])],
        accComplete: Boolean,
    ): Result[A] = cases match
      case (bool, a) :: next =>
        val complete = bool.get.complete && accComplete

        bool.get.value match
          case Some(true) => if (complete) a.get else a.get.asPlaceholder
          case _          => resultRecurse(next, complete)
      case Nil => Result.Incomplete

    def thunk(
        cases: List[(Thunk[Result[Boolean]], Thunk[Result[A]])],
    ): Thunk[Result[A]] =
      Thunk(() => result(cases))

    def explain(
        cases: List[(Thunk[Result[Boolean]], Explanation, Explanation)],
    ): Explanation =
      explainRecurse(cases, Explanation.Operation(List()))

    @annotation.tailrec
    def explainRecurse(
        cases: List[(Thunk[Result[Boolean]], Explanation, Explanation)],
        explanation: Explanation,
    ): Explanation = cases match
      case (bool, boolExp, aExp) :: next =>
        bool.get match
          case Result.Complete(true) =>
            Explanation.Operation(
              explanation.children :+ List(boolExp, aExp),
            )
          case Result.Complete(false) =>
            explainRecurse(
              next,
              Explanation.Operation(explanation.children :+ List(boolExp)),
            )
          case _ =>
            Explanation.Operation(
              explanation.children :+ List(boolExp),
            )
      case Nil => explanation

  private def extract[X](
      f: (source: Result[X]) => Result[A],
  )(using fact: Factual): MaybeVector[Result[A]] =
    val parent = fact(PathItem.Parent)(0).get
    for {
      result <- parent.get
    } yield f(result.asInstanceOf[Result[X]])

  private def extractThunk[X](
      f: (source: Result[X]) => Result[A],
  )(using fact: Factual): MaybeVector[Thunk[Result[A]]] =
    val parent = fact(PathItem.Parent)(0).get
    for {
      thunk <- parent.getThunk
    } yield for {
      result <- thunk
    } yield f(result.asInstanceOf[Result[X]])

  def collect[X](path: Path, x: Expression[X], op: CollectOperator[A, X])(using
      fact: Factual,
  ): Result[A] =
    val vect = for {
      item <- fact(path :+ PathItem.Wildcard)
      thunk <- x.getThunk(using item.get)
    } yield (item.get.get(0).get.asInstanceOf[CollectionItem], thunk)

    op(vect)

  def conditionSet(list: List[(Expression[Boolean], Expression[String])])(using
      fact: Factual,
  ): Result[List[String]] =
    val thunks = list.map(o => (o._1.getThunk, o._2.getThunk))

    // If any predicates or options are incomplete, we return Incomplete.
    if (thunks.exists((b, s) => b(0).get.complete == false || s(0).get.complete == false)) {
      return Result.Incomplete
    }

    val strings = for {
      f <- thunks.filter((b, s) => {
        // if a boolean node is false, we exclude its
        // value from the returned list.
        b(0).get.complete && b(0).get.get == true &&
        // Similarly, if the value we're returning is incomplete, we exclude it.
        s(0).get.complete
      })
    } yield {
      f._2(0).get.get
    }
    Result.Complete(strings.toList)

  def conditionExplain(list: List[(Expression[Boolean], Expression[String])])(using
      fact: Factual,
  ): MaybeVector[Explanation] =
    // First we call explain on each bool and string
    val explanations = for (bool, str) <- list yield (bool.explain, str.explain)

    // The result is a list of tuples of MaybeVectors, so we need to vectorize
    // this operation, which flattens the lists of tuples and returns a
    // MaybeVector of an Explanation, with the flattened lists as its children
    MaybeVector.vectorizeListTuple2(
      (expList: List[(Explanation, Explanation)]) =>
        Explanation.opWithInclusiveChildren(
          expList.flatten { case (a, b) => List(a, b) },
        ),
      explanations,
    )
