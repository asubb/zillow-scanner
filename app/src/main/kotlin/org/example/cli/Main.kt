package org.example.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.core.main

class ZillowScannerCommand : CliktCommand(name = "zillow-scanner") {
    override fun run() = Unit
}

fun main(args: Array<String>) = ZillowScannerCommand()
    .subcommands(ScanCommand())
    .main(args)
