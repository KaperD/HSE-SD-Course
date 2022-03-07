package ru.hse

import ru.hse.cli.CLIImpl
import ru.hse.command.*
import ru.hse.environment.Environment
import ru.hse.environment.EnvironmentImpl
import ru.hse.executor.ExpressionExecutorImpl
import ru.hse.factory.CommandFactory
import ru.hse.factory.CommandFactoryImpl
import ru.hse.factory.PipeFactoryImpl
import ru.hse.parser.AssignmentParser
import ru.hse.parser.PipeParser
import ru.hse.splitter.PipeSplitterImpl
import ru.hse.substitutor.SubstitutorImpl
import ru.hse.tokenizer.TokenizerImpl
import ru.hse.validator.VarNameValidatorImpl

fun main() {
    run()
}

fun run() {
    val tokenizer = TokenizerImpl()
    val varNameValidator = VarNameValidatorImpl()
    val environment = EnvironmentImpl(EnvironmentImpl(null, System.getenv()))
    val substitutor = SubstitutorImpl(environment, varNameValidator)
    val pipeSplitter = PipeSplitterImpl()
    val assignmentParser = AssignmentParser(varNameValidator, tokenizer, substitutor)
    val pipeParser = PipeParser(tokenizer, pipeSplitter, substitutor)
    val commandFactory = CommandFactoryImpl(environment)
    addHseshCommands(environment, commandFactory)
    val pipeFactory = PipeFactoryImpl()
    val expressionExecutor = ExpressionExecutorImpl(
        assignmentParser,
        pipeParser,
        environment,
        commandFactory,
        pipeFactory
    )
    val cli = CLIImpl(System.`in`, System.out, System.err)
    val app = HseshApplication(cli, expressionExecutor)
    app.run()
}

fun addHseshCommands(environment: Environment, commandFactory: CommandFactory) {
    commandFactory.registerCommand("echo") { EchoCommand(it) }
    commandFactory.registerCommand("wc") { WcCommand(environment, it) }
    commandFactory.registerCommand("cat") { CatCommand(environment, it) }
    commandFactory.registerCommand("pwd") { PwdCommand(environment, it) }
    commandFactory.registerCommand("exit") { ExitCommand() }
    commandFactory.registerCommand("grep") { GrepCommand(environment, it) }
    commandFactory.registerCommand("ls") { LsCommand(environment, it) }
    commandFactory.registerCommand("cd") { CdCommand(environment, it) }
}
