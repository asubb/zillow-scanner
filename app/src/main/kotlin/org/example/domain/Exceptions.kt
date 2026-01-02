package org.example.domain

class BrowserConnectionException(message: String, cause: Throwable? = null) : Exception(message, cause)

class AuthenticationException(message: String, cause: Throwable? = null) : Exception(message, cause)

class SpreadsheetNotFoundException(message: String, cause: Throwable? = null) : Exception(message, cause)
