.PHONY: dev
dev:
	sbt '~ compile;fastOptJS;publishLocal'

.PHONY: publish
publish:
	sbt compile fastOptJS publishLocal

.PHONY: ci_publish
ci_publish:
	sbt -error compile publishLocal

.PHONY: format
format:
	sbt scalafmtAll

.PHONY: format_check
format_check:
	sbt scalafmtCheckAll

.PHONY: ci_format_check
ci_format_check:
	sbt -error scalafmtCheckAll

.PHONY: test
test:
	sbt test

# Once we upgrade to the next 3.x.x LTS version, Change this to 
# sbt -info 'set Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oC")' test
.PHONY: ci_test
ci_test:
	sbt -error test && echo 'All tests passed.'
	