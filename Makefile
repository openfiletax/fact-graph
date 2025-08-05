.PHONY: dev
dev:
	sbt '~ compile;fastOptJS;publishLocal'

.PHONY: publish
publish:
	sbt compile fastOptJS publishLocal

.PHONY: test
test:
	sbt test
