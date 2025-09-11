.PHONY: dev
dev:
	sbt '~ compile;fastOptJS;publishLocal'



.PHONY: publish
publish:
	sbt compile fastOptJS publishLocal

.PHONY: ci_publish
ci_publish:
	sbt -error compile publishLocal

# Security scanning, formatting, and testing targets

.PHONY: \
		format format_check ci_format_check \
		test ci_test \
		ci_semgrep_scala

# --- Formatting ---

format:
	sbt scalafmtAll

format_check:
	sbt scalafmtCheckAll

ci_format_check:
	sbt -error scalafmtCheckAll

# --- Testing ---

test:
	sbt test

# Once we upgrade to the next 3.x.x LTS version, Change this to 
# sbt -info 'set Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oC")' test
ci_test:
	sbt -error test && echo 'All tests passed.'

# --- Security Scanning ---

ci_security_scan:
	semgrep scan --verbose \
		--metrics off \
		--config p/security-audit \
		--config p/scala \
		--severity WARNING \
		--error
