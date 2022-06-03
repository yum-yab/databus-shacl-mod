package org.dbpedia.models

import org.apache.jena.graph.Graph
import org.apache.jena.shacl.Shapes
import org.apache.jena.shacl.ValidationReport

data class ShaclCheck(val name: String, val shape: Shapes)

data class DataGraph(val sourceURI: String, val graph: Graph)

data class ValidationResult(val sourceURI: String, val report: ValidationReport?)