# Table of Contents

1. [[Setup](#setup)](#setup)
   1.1 [[Developer Environment Setup](#developer-environment-setup)](#developer-environment-setup)
        1.1.1 [[Dependencies](#dependencies)](#dependencies)
           - [[Java and Scala Setup](#java-and-scala-setup)](#java-and-scala-setup)
2. [[General Usage](#general-usage)](#general-usage)
3. [[IDE Support](#ide-support)](#ide-support)
   3.1 [[IntelliJ](#intellij)](#intellij)
   3.2 [[VSCode](#vscode)](#vscode)
   3.3 [[Vim / Nvim](#vim--nvim)](#vim--nvim)
4. [[Before Your First Commit](#before-your-first-commit)](#before-your-first-commit)
   4.1 [[Configure Commit Email Address](#configure-commit-email-address)](#configure-commit-email-address)

## Developer Environment Setup

### Dependencies

#### Java and Scala Setup
The Fact Graph is a Scala project, so it requires Scala, a JDK, and sbt (scala build tool).

If you don't already have java and sbt installed, you can do so using coursier.

1. [Install coursier](https://get-coursier.io/docs/cli-installation)
```bash

# Linux x86-64 (aka AMD64)
curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > cs
# Linux ARM64
curl -fL "https://github.com/VirtusLab/coursier-m1/releases/latest/download/cs-aarch64-pc-linux.gz" | gzip -d > cs

# Once downloaded for your system, run setup
chmod +x cs
./cs setup

# macOS Apple Silicon (M1, M2, ...):
$ curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-aarch64-apple-darwin.gz | gzip -d > cs
# macOS Intel:
$ curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-apple-darwin.gz | gzip -d > cs

# Once downloaded for your system, run setup
$ chmod +x cs
$ ./cs setup

# Alternatively you can use Homebrew on macOS:
$ brew install coursier/formulas/coursier
$ cs setup

```

2. Install Java v21.0.5
```bash
cs java install openjdk:21.0.5
```

3. Install sbt
```bash
cs install sbt
```

4. Install scalafmt
```bash
cs install scalafmt
```

Depending on how much Java stuff you already have on your local development machine, you may need to ensure that sbt is able to find the JDK.

```sh
brew link --force openjdk
export JAVA_HOME=$(brew --prefix openjdk)
```

A NodeJS installation is also required to run the test suite against the JS build.

## General Usage

* `sbt compile` - compile the JVM build
* `sbt fastOptJS` - compile the JS build quickly
* `sbt fullOptJS` - compile the JS build for production
* `sbt clean` - delete the build artifacts
* `sbt test` - run the tests (both Java and JS)
* `sbt testOnly tinSpec.scala` - run just the `tinSpec.scala` test
* `sbt publishLocal` - publish to your local repository to use Fact Graph with other projects

Some commands depend on other commands.
For instance, `sbt test` will run `sbt fastOptJS` to compile the JS build, if necessary.

Like maven, sbt allows you to chain commands.
To run the tests from a completely fresh build, you can run `sbt clean test`


### IDE Support

#### IntelliJ

With the scala extension installed, IntelliJ will give you a note to enable "nightly" mode to take advantage of the
latest features.
You should do this.

If you run into issues, running `sbt compile` from "Run Anything" and then clicking "Sync all sbt Projects" typically
resolves things.

To enable format on save:
1. Open Preferences and search for `scalafmt`. Go to `Editor` -> `Code Style` -> `Scala`.
2. Select `Scalafmt` in the `Formatter` menu.
3. Within the same Preferences window, search for `actions on save`. Go to `Tools` -> `Actions on Save`.
4. Ensure `Reformat code` is selected.

#### VSCode

1. Install Metals extension. Search for and install _Metals_ from the _Extensions Marketplace_.
2. To enable format on save: Open the Command Palette using `Ctrl/Cmd + Shift + P` ->
   `Preferences: Open Workspace Settings (JSON)` and add the following block:

_Note 1: there can be only one global JSON object, if you already have settings, you will just be adding the rules
inside of the global object to the existing global object._

_Note 2: the editorconfig.* settings are in here because Editor Config is a suggested extension for this project._

  ```json
    {
  "[scala]": {
    "editor.formatOnSave": true,
    "editor.defaultFormatter": "scalameta.metals",
    "editor.formatOnSaveMode": "file"
  },
  "metals.enableScalafmt": true,
  "editorconfig.enable": true,
  "editorconfig.exclude": [
    "**/*.scala"
  ]
}
  ```

#### Vim / Nvim

1. Install Coursier

```bash
brew install coursier
eval "$(coursier java --jvm 21 --env)" # you may not need this line
```

2. Use Coursier to install the following tools

```bash
# Install scalafmt
coursier install scalafmt

# Install the Metals language server
coursier install metals
```

3. Vim - add the following to your `~/.vimrc`:
```bash
autocmd BufWritePre *.scala call s:scalafmt()

function! s:scalafmt()
  let l:cmd = 'scalafmt ' . shellescape(expand('%:p'))
  let l:output = system(l:cmd)
  if v:shell_error
    echohl ErrorMsg | echo "Scalafmt failed:" l:output | echohl None
  else
    # Reload the file silently to avoid W11 warnings
    silent! edit!
  endif
endfunction

```
4. Nvim - add the following to your `init.lua`:

```bash
vim.api.nvim_create_autocmd("BufWritePre", {
  pattern = "*.scala",
  callback = function()
    local file = vim.fn.expand("%:p")
    local result = vim.system({ "scalafmt", file }):wait()

    if result.code ~= 0 then
      vim.notify("Scalafmt failed:\n" .. result.stderr, vim.log.levels.ERROR)
    else
      -- Reload the file silently to avoid W11 warnings
      vim.cmd("silent! edit!")
    end
  end,
})
```

## Before your first commit

### Configure commit email address

All commits from IRS staff on this project must be authored using your GitHub-provided `noreply` email address.

To achieve this, you'll need to make sure that commits authored via the GitHub web UI as well as local `git` use the GitHub-provided `noreply` email address:

1. Enable private email address
    1. Go to [your GitHub email settings](https://github.com/settings/emails)
    2. Enable `Keep my email address private`
    3. Note and copy the GitHub email address that is displayed. It looks like `{ID}+{USERNAME}@users.noreply.github.com` ([reference](https://docs.github.com/en/account-and-profile/reference/email-addresses-reference#your-noreply-email-address))
    4. Enable `Block command line pushes that expose my email` (Optional)
2. Set your local `git` email address for authoring commits
    1. Configure your local `git` `user.email` setting
        * If you would like to use your no-reply email address for _all_ local development, run
           ```shell
           git config --global user.email "YOUR_NO_REPLY_EMAIL"
           ```
        * If you prefer to only use the no-reply email address for development in your Fact Graph working copy, run
          ```shell
          # Ensure you're in the directory for this git project
          cd ./path/to/fact-graph/
          # Set your git email config for this repository only
          git config user.email "YOUR_NO_REPLY_EMAIL"
          ```
    2. Verify the configuration by having `git` echo the `user.email` configuration
       ```shell
       $ git config --global user.email
       YOUR_ID+YOUR_USERNAME@users.noreply.github.com
       ```

For more details, see:
* [Setting your commit email address](https://docs.github.com/en/account-and-profile/how-tos/setting-up-and-managing-your-personal-account-on-github/managing-email-preferences/setting-your-commit-email-address#setting-your-email-address-for-a-single-repository).
* [Your no-reply email address](https://docs.github.com/en/account-and-profile/reference/email-addresses-reference#your-noreply-email-address)

