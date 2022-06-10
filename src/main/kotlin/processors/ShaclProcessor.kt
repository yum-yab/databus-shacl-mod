package org.dbpedia.processors

import kotlinx.coroutines.*
import org.apache.jena.graph.Graph
import org.apache.jena.riot.RDFLanguages
import org.apache.jena.riot.RDFParser
import org.apache.jena.riot.system.ErrorHandlerFactory
import org.apache.jena.shacl.ShaclValidator
import org.dbpedia.databus_mods.lib.util.UriUtil
import org.dbpedia.databus_mods.lib.worker.execution.Extension
import org.dbpedia.databus_mods.lib.worker.execution.ModProcessor
import org.dbpedia.models.DataGraph
import org.dbpedia.models.ShaclCheck
import org.dbpedia.models.ValidationResult
import org.dbpedia.utils.Utils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI


@Component
class ShaclProcessor: ModProcessor {

    private val logger = LoggerFactory.getLogger(this::class.simpleName)

    private val checks: List<ShaclCheck> = Utils.loadShapes()


    override fun process(extension: Extension?) {
        extension!!
        logger.info("Started process for file: ${extension.source()}")
        extension.setType("http://mods.tools.dbpedia.org/ns/demo#ArchivoSHACLMod")

        val dataGraph = loadGraph(URI(extension.source()))

        runBlocking {
            val jobs = checks.map {
                async {
                    validate(it, dataGraph)
                }
            }
            val results = jobs.awaitAll()

            // create entries in model
            results.forEach {

            }

            // write results
            results.filter { it.report != null  }.forEach {
                it.report!!
                val filename = it.check.name + "-report.ttl"
                it.report.model.write(extension.createModResult(filename, "http://dataid.dbpedia.org/ns/mod#enrichmentDerivedFrom"), RDFLanguages.TURTLE.toString())
            }
        }


    }


    private fun loadGraph(uri: URI): DataGraph {
        val inputStream = UriUtil.openStream(uri)

        val graph = Graph.emptyGraph
        try {
            inputStream.use {
                RDFParser.create()
                    .source(it)
                    .lang(RDFLanguages.TTL)
                    .errorHandler(ErrorHandlerFactory.errorHandlerWarn)
                    .parse(graph)
            }
        } catch (ex: java.lang.Exception) {
            logger.error("Could not load file from databus: $uri", ex)
        }

        return DataGraph(uri.toString(), graph)
    }

    private fun validate(check: ShaclCheck, dataGraph: DataGraph): ValidationResult {
        return try {
            val report = ShaclValidator.get().validate(check.shape, dataGraph.graph)
            ValidationResult(report, check)
        } catch (ex: java.lang.Exception) {
            logger.error("Error checking ${dataGraph.sourceURI} with ${check.name}", ex)
            ValidationResult(null, check, error = ex.toString())
        }
    }
}