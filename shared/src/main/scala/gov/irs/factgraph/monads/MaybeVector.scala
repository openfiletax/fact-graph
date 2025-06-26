package gov.irs.factgraph.monads

enum MaybeVector[+A]:
  case Single(x: A)
  case Multiple(vect: Vector[A], c: Boolean)

  def apply(i: Int): A = this match
    case Single(x)         => x
    case Multiple(vect, _) => vect(i)

  def toVector: Vector[A] = this match
    case Single(x)         => Vector(x)
    case Multiple(vect, _) => vect

  def toList: List[A] = this match
    case Single(x)         => List(x)
    case Multiple(vect, _) => vect.toList

  def length: Option[Int] = this match
    case Single(_)         => None
    case Multiple(vect, _) => Some(vect.length)

  def complete: Boolean = this match
    case Single(_)      => true
    case Multiple(_, c) => c

  def map[B](f: A => B): MaybeVector[B] = this match
    case Single(x)         => MaybeVector(f(x))
    case Multiple(vect, c) => MaybeVector(vect.map(f), c)

  def flatMap[B](f: A => MaybeVector[B]): MaybeVector[B] = this match
    case Single(x) => f(x)
    case Multiple(vect, c) =>
      val mvs = vect.map(f)
      val complete = c && mvs.forall(_.complete)

      MaybeVector(mvs.flatMap(mv => mv.toVector), complete)

  def foreach(f: A => Unit): Unit = this match
    case Single(x)          => f(x)
    case Multiple(vect, c_) => vect.map(f)

  def toMultiple: MaybeVector[A] = this match
    case Single(x) => MaybeVector(Vector(x), true)
    case _         => this

object MaybeVector:
  def apply[A](x: A): MaybeVector[A] = MaybeVector.Single(x)
  def apply[A](s: Seq[A], complete: Boolean): MaybeVector[A] =
    MaybeVector.Multiple(s.toVector, complete)

  def vectorizeList[A, B, X](
      f: (h: A, t: List[B]) => X,
      head: MaybeVector[A],
      tail: List[MaybeVector[B]],
  ): MaybeVector[X] =
    val all = head +: tail
    val uniqueSizes = all.flatMap(_.length).distinct

    uniqueSizes match
      case Nil => MaybeVector(f(head(0), tail.map(_(0))))
      case size :: Nil =>
        val results =
          for i <- 0 until size
          yield f(head(i), tail.map(_(i)))

        val complete = all.forall(_.complete)

        MaybeVector(results, complete)
      case _ =>
        throw new UnsupportedOperationException(
          "cannot operate on vectors of different lengths",
        )

  def vectorize2[A, B, X](
      f: (leftHS: A, rightHS: B) => X,
      lhs: MaybeVector[A],
      rhs: MaybeVector[B],
  ): MaybeVector[X] =
    val uniqueSizes = List(lhs, rhs).flatMap(_.length).distinct

    uniqueSizes match
      case Nil => MaybeVector(f(lhs(0), rhs(0)))
      case size :: Nil =>
        val results =
          for i <- 0 until size
          yield f(lhs(i), rhs(i))

        val complete = lhs.complete && rhs.complete

        MaybeVector(results, complete)
      case _ =>
        throw new UnsupportedOperationException(
          "cannot operate on vectors of different lengths",
        )

  def vectorize4[A, B, C, D, X](
      f: (arg1: A, arg2: B, arg3: C, Arg4: D) => X,
      arg1: MaybeVector[A],
      arg2: MaybeVector[B],
      arg3: MaybeVector[C],
      arg4: MaybeVector[D],
  ): MaybeVector[X] =
    val uniqueSizes = List(arg1, arg2, arg3, arg4).flatMap(_.length).distinct

    uniqueSizes match
      case Nil => MaybeVector(f(arg1(0), arg2(0), arg3(0), arg4(0)))
      case size :: Nil =>
        val results =
          for i <- 0 until size
          yield f(arg1(i), arg2(i), arg3(i), arg4(i))

        val complete =
          arg1.complete && arg2.complete && arg3.complete && arg4.complete

        MaybeVector(results, complete)
      case _ =>
        throw new UnsupportedOperationException(
          "cannot operate on vectors of different lengths",
        )

  def vectorizeListTuple2[A, B, X](
      f: (casesList: List[(A, B)]) => X,
      cases: List[(MaybeVector[A], MaybeVector[B])],
  ): MaybeVector[X] =
    val uniqueSizes = cases
      .flatMap((a, b) => List(a.length, b.length))
      .flatten
      .distinct

    uniqueSizes match
      case Nil => MaybeVector(f(cases.map((a, b) => (a(0), b(0)))))
      case size :: Nil =>
        val results =
          for i <- 0 until size
          yield f(cases.map((a, b) => (a(i), b(i))))

        val complete = cases.forall((a, b) => a.complete && b.complete)

        MaybeVector(results, complete)
      case _ =>
        throw new UnsupportedOperationException(
          "cannot operate on vectors of different lengths",
        )

  def vectorizeListTuple3[A, B, C, X](
      f: (casesList: List[(A, B, C)]) => X,
      cases: List[(MaybeVector[A], MaybeVector[B], MaybeVector[C])],
  ): MaybeVector[X] =
    val uniqueSizes = cases
      .flatMap((a, b, c) => List(a.length, b.length, c.length))
      .flatten
      .distinct

    uniqueSizes match
      case Nil => MaybeVector(f(cases.map((a, b, c) => (a(0), b(0), c(0)))))
      case size :: Nil =>
        val results =
          for i <- 0 until size
          yield f(cases.map((a, b, c) => (a(i), b(i), c(i))))

        val complete =
          cases.forall((a, b, c) => a.complete && b.complete && c.complete)

        MaybeVector(results, complete)
      case _ =>
        throw new UnsupportedOperationException(
          "cannot operate on vectors of different lengths",
        )
