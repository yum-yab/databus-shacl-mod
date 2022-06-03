package org.dbpedia.processors

import kotlinx.coroutines.*
import org.apache.jena.graph.Graph
import org.apache.jena.shacl.ShaclValidator
import org.dbpedia.databus_mods.lib.worker.execution.Extension
import org.dbpedia.databus_mods.lib.worker.execution.ModProcessor
import org.dbpedia.models.DataGraph
import org.dbpedia.models.ShaclCheck
import org.dbpedia.models.ValidationResult
import org.dbpedia.utils.HelperFunctions
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ShaclProcessor: ModProcessor {

    private val logger = LoggerFactory.getLogger(ShaclProcessor::class.simpleName)

    private val checks: List<ShaclCheck> = HelperFunctions.loadShapes()


    override fun process(extension: Extension?) {
        val checks = emptyList<ShaclCheck>()
        val dataGraph = DataGraph("testytest", Graph.emptyGraph)


        runBlocking {
            val jobs = checks.map {
                async {
                    validate(it, dataGraph)
                }
            }
            val results = jobs.awaitAll()
        }
    }


    private fun validate(check: ShaclCheck, dataGraph: DataGraph): ValidationResult {
        return try {
            val report = ShaclValidator.get().validate(check.shape, dataGraph.graph)
            ValidationResult(dataGraph.sourceURI, report)
        } catch (ex: java.lang.Exception) {
            logger.error("Error checking ${dataGraph.sourceURI} with ${check.name}", ex)
            ValidationResult(dataGraph.sourceURI, null)
        }
    }
}