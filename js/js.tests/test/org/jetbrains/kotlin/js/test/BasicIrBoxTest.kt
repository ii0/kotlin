/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test

import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.backend.js.Result
import org.jetbrains.kotlin.ir.backend.js.compile
import org.jetbrains.kotlin.js.config.JsConfig
import org.jetbrains.kotlin.js.facade.MainCallParameters
import org.jetbrains.kotlin.js.facade.TranslationUnit
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.test.TargetBackend
import java.io.File

private val runtimeSources = listOfKtFilesFrom(
    "libraries/stdlib/js/src/kotlin/core.kt",
    "core/builtins/native/kotlin/Number.kt",
    "core/builtins/native/kotlin/Comparable.kt",
    "libraries/stdlib/js/irRuntime",
    BasicBoxTest.COMMON_FILES_DIR_PATH
)

private var runtimeResult: Result? = null
private var runtimeFile: File? = null

abstract class BasicIrBoxTest(
    pathToTestDir: String,
    testGroupOutputDirPrefix: String,
    pathToRootOutputDir: String = BasicBoxTest.TEST_DATA_DIR_PATH,
    generateSourceMap: Boolean = false,
    generateNodeJsRunner: Boolean = false
) : BasicBoxTest(
    pathToTestDir,
    testGroupOutputDirPrefix,
    pathToRootOutputDir = pathToRootOutputDir,
    typedArraysEnabled = true,
    generateSourceMap = generateSourceMap,
    generateNodeJsRunner = generateNodeJsRunner,
    targetBackend = TargetBackend.JS_IR
) {

    override var skipMinification = true

    override fun translateFiles(
        units: List<TranslationUnit>,
        outputFile: File,
        config: JsConfig,
        outputPrefixFile: File?,
        outputPostfixFile: File?,
        mainCallParameters: MainCallParameters,
        incrementalData: IncrementalData,
        remap: Boolean,
        testPackage: String?,
        testFunction: String
    ) {
        val filesToCompile = units
            .map { (it as TranslationUnit.SourceFile).file }
            .filterNot { it.virtualFilePath.contains("/js/js.translator/testData/_commonFiles/") }

        if (runtimeResult == null) {
            val myConfiguration = config.configuration.copy()

            myConfiguration.languageVersionSettings = LanguageVersionSettingsImpl(LanguageVersion.LATEST_STABLE, ApiVersion.LATEST_STABLE)

            runtimeResult = compile(config.project, runtimeSources.map(::createPsiFile), myConfiguration)
            runtimeFile = File(testGroupOutputDirForCompilation, "testRuntime.js").apply {
                writeText(runtimeResult!!.generatedCode)
            }
        }

        val result = compile(
            config.project,
            filesToCompile,
            config.configuration,
            FqName((testPackage?.let { "$it." } ?: "") + testFunction),
            listOf(runtimeResult!!.moduleDescriptor))

        outputFile.parentFile.mkdirs()
        outputFile.writeText(result.generatedCode)
    }

    override fun runGeneratedCode(
        jsFiles: List<String>,
        testModuleName: String?,
        testPackage: String?,
        testFunction: String,
        expectedResult: String,
        withModuleSystem: Boolean
    ) {
        // TODO: should we do anything special for module systems?
        // TODO: cache runtime.js and don't load kotlin.js & co for IR
        super.runGeneratedCode(listOf(runtimeFile!!.path) + jsFiles, null, null, testFunction, expectedResult, false)
    }
}

private fun listOfKtFilesFrom(vararg paths: String): List<String> {
    val currentDir = File(".")
    return paths.flatMap { path ->
        File(path)
            .walkTopDown()
            .filter { it.extension == "kt" }
            .map { it.relativeToOrSelf(currentDir).path }
            .asIterable()
    }
}