package org.dbpedia.models

import org.apache.jena.graph.Graph
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.shacl.Shapes
import org.apache.jena.shacl.ValidationReport

data class ShaclCheck(val name: String, val shape: Shapes)

data class DataGraph(val sourceURI: String, val graph: Graph)

data class ValidationResult(val report: ValidationReport?, val check: ShaclCheck, val error: String? = null) {

    fun metadataModel(): Model {
        //TODO: crete fun and ontological data for shacl results
        val model = ModelFactory.createDefaultModel()
        return model
    }
}

