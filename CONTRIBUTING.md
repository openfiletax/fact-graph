# Contributing

* If you are a member of the public, see [members of the public](#members-of-the-public)
* If you are an active maintainer, see [maintainers](#maintainers)

## Members of the public

The primary purpose of this repo being open source is to share with taxpayers transparently how this tool works.

Though you are welcome to open Pull Requests, we are not accepting external contributions at this time.

This codebase is dedicated to the public domain under the [Creative Commons Zero v1.0 Universal](LICENSE.md) license (CC0 1.0).

## Maintainers

This repository lives in the public domain in the United States (see [License](LICENSE.md)). Therefore, details about the repository _and its contributors_ are visible to the public.

In this model, individual maintainers are responsible for the quality of the code, commits, and other contributions to the repository.

For further understanding of the rationale behind open source, see [Open Source Benefits](./docs/oss/benefits.md).

### Know what is public

As a contributor to this repository, assume that any contribution and associated metadata is visible to the public. This includes:
* Contents of commits (files added, changed, or removed)
* Commit messages
* Pull requests
* Issues
* Comments
* Commit authors (name, email address, GitHub username)

Individual contributors are responsible for sharing only what they are comfortable with making available to the public domain.
As a result, individuals should configure their accounts and git configurations accordingly.

As a matter of policy, the IRS requires that internal contributors use their GitHub `no-reply` email address for authoring commits. See [ONBOARDING - Configure Commit Email Address](/ONBOARDING.md#configure-commit-email-address). 

> [!WARNING]
> Sensitive PII and [SBU](https://en.wikipedia.org/wiki/Sensitive_but_unclassified) are not permitted _anywhere_ in open source repositories.
>
> Any accidental exposure of SBU or PII must be immediately reported and remediated as an incident

### Best Practices

* Be intentional about good git hygiene, if not for the open source community for yourself and your fellow maintainers.
* Prefer the use of other Free and Open Source Software (FOSS) to expand and contribute back to the community.
* Use automated tooling for code formatting and basic linting.
* Enforce code accuracy and test health with automated checks for passing test coverage.
* Use only fake data or leverage "faker"-style libraries for generating realistic data for tests.
* Supplement automated tests/checks with thoughtful code review from peers.
* Perfection should not be the enemy of the good. Instead, iterations should aim for "better".
