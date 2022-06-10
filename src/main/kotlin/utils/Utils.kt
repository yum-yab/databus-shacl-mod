package org.dbpedia.utils

import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.shacl.Shapes
import org.dbpedia.models.ShaclCheck
import org.slf4j.LoggerFactory
import java.nio.file.Paths

object Utils {

    private val logger = LoggerFactory.getLogger(this::class.simpleName)

    fun loadShapes(): List<ShaclCheck> {
        val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
        val shaclLibPath = Paths.get(projectDirAbsolutePath, "/shacl-lib")
        val files = shaclLibPath.toFile().walk()
            .filter { file -> file.isFile and file.exists() }
            .filter { file -> file.path.endsWith(".ttl") }

        val shapes = files.map { file -> try {
                val shape = Shapes.parse(RDFDataMgr.loadGraph(file.absolutePath))
                ShaclCheck(file.name.reversed().split(".", limit = 2)[0].reversed(), shape)
            } catch (ex: java.lang.Exception) {
                logger.error("Could not load shape $file :", ex)
                null
            }
        }.filterNotNull().toList()

        logger.info("Loaded ${shapes.size} shapes: ${shapes.map { it.name }}")

        return shapes
    }

}