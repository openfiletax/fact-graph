package gov.irs.factgraph

import org.scalatest.funspec.AnyFunSpec

class ExplanationSpec extends AnyFunSpec:
  describe("Explanation") {
    describe(".solves") {
      val x = Explanation.Operation(
        List(
          List(
            Explanation.Writable(false, Path("/a")),
            Explanation.Writable(false, Path("/b"))
          ),
          List(
            Explanation.Writable(false, Path("/c")),
            Explanation.Writable(false, Path("/d"))
          )
        )
      )
      val y = Explanation.Operation(
        List(
          List(
            Explanation.Writable(false, Path("/e")),
            Explanation.Writable(false, Path("/f"))
          )
        )
      )
      val z = Explanation.Operation(
        List(
          List(
            Explanation.Writable(false, Path("/g"))
          ),
          List(
            Explanation.Writable(false, Path("/h"))
          )
        )
      )

      it("solves unnested explanations") {
        assert(
          x.solves == List(
            List(Path("/a"), Path("/b")),
            List(Path("/c"), Path("/d"))
          )
        )
      }

      it("solves nested explanations") {
        assert(
          Explanation.Operation(List(List(x))).solves == List(
            List(Path("/a"), Path("/b")),
            List(Path("/c"), Path("/d"))
          )
        )
      }

      it("combines exclusively nested explanations") {
        assert(
          Explanation
            .Operation(
              List(
                List(x),
                List(y)
              )
            )
            .solves == List(
            List(Path("/a"), Path("/b")),
            List(Path("/c"), Path("/d")),
            List(Path("/e"), Path("/f"))
          )
        )

        assert(
          Explanation
            .Operation(
              List(
                List(x),
                List(z)
              )
            )
            .solves == List(
            List(Path("/a"), Path("/b")),
            List(Path("/c"), Path("/d")),
            List(Path("/g")),
            List(Path("/h"))
          )
        )
      }

      it("combines inclusively nested explanations") {
        assert(
          Explanation
            .Operation(
              List(
                List(x, y)
              )
            )
            .solves == List(
            List(Path("/a"), Path("/b"), Path("/e"), Path("/f")),
            List(Path("/c"), Path("/d"), Path("/e"), Path("/f"))
          )
        )

        assert(
          Explanation
            .Operation(
              List(
                List(x, z)
              )
            )
            .solves == List(
            List(Path("/a"), Path("/b"), Path("/g")),
            List(Path("/a"), Path("/b"), Path("/h")),
            List(Path("/c"), Path("/d"), Path("/g")),
            List(Path("/c"), Path("/d"), Path("/h"))
          )
        )

        assert(
          Explanation
            .Operation(
              List(
                List(x, y, z)
              )
            )
            .solves == List(
            List(Path("/a"), Path("/b"), Path("/e"), Path("/f"), Path("/g")),
            List(Path("/a"), Path("/b"), Path("/e"), Path("/f"), Path("/h")),
            List(Path("/c"), Path("/d"), Path("/e"), Path("/f"), Path("/g")),
            List(Path("/c"), Path("/d"), Path("/e"), Path("/f"), Path("/h"))
          )
        )
      }

      it("combines inclusively and exclusively nested explanations") {
        assert(
          Explanation
            .Operation(
              List(
                List(x, y),
                List(z)
              )
            )
            .solves == List(
            List(Path("/a"), Path("/b"), Path("/e"), Path("/f")),
            List(Path("/c"), Path("/d"), Path("/e"), Path("/f")),
            List(Path("/g")),
            List(Path("/h"))
          )
        )
      }
    }

    describe(".incompleteDependencies") {
      it("traverses the explanation to find the incomplete dependencies") {
        val test = Explanation.Operation(
          List(
            List(
              Explanation
                .Dependency(false, Path("/test"), Path("../missing"), List()),
              Explanation.Dependency(
                true,
                Path("/test"),
                Path("../found"),
                List(
                  List(
                    Explanation.Dependency(
                      false,
                      Path("/found"),
                      Path("../missing"),
                      List()
                    ),
                    Explanation.Dependency(
                      false,
                      Path("/found"),
                      Path("../notHere"),
                      List()
                    )
                  )
                )
              )
            )
          )
        )

        val result = test.incompleteDependencies

        assert(result.contains((Path("/test"), Path("../missing"))))
        assert(result.contains((Path("/found"), Path("../missing"))))
        assert(result.contains((Path("/found"), Path("../notHere"))))

        assert(result.length == 3)
      }
    }
  }
