package xyz.malefic.navigate.config

import androidx.compose.runtime.Composable
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.w3c.dom.Node
import xyz.malefic.navigate.DynamicRoute
import xyz.malefic.navigate.Route
import xyz.malefic.navigate.StaticRoute

/** A loader that reads route configurations from an XML input stream. */
class XmlConfigLoader : ConfigLoader {
  /**
   * Loads routes from an XML input stream and maps them to composable functions. Returns a pair
   * with the startup route as a string and the list of routes.
   *
   * @param composableMap A map of route names to composable functions.
   * @param inputStream The input stream containing the XML configuration.
   * @return A pair containing the startup route and a list of routes.
   */
  override fun loadRoutes(
    composableMap: Map<String, @Composable (List<String?>) -> Unit>,
    inputStream: InputStream,
  ): Pair<String, List<Route>> {
    val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val document = documentBuilder.parse(inputStream)
    document.documentElement.normalize()

    val routes = mutableListOf<Route>()
    val routeNodes = document.getElementsByTagName("route")

    for (i in 0 until routeNodes.length) {
      val node = routeNodes.item(i)
      if (node.nodeType == Node.ELEMENT_NODE) {
        val element = node as Element
        val name = element.getElementsByTagName("name").item(0).textContent
        val composableName = element.getElementsByTagName("composable").item(0).textContent
        val composable =
          composableMap[composableName] ?: { _ -> androidx.compose.material.Text("Unknown route") }
        val hidden =
          element.getElementsByTagName("hidden").item(0)?.textContent?.toBoolean() ?: false
        val params = element.getElementsByTagName("param")
        val paramList = mutableListOf<String>()
        for (j in 0 until params.length) {
          paramList.add(params.item(j).textContent)
        }
        if (paramList.isNotEmpty()) {
          routes.add(DynamicRoute(name, composable, hidden, paramList))
        } else {
          routes.add(StaticRoute(name, composable, hidden))
        }
      }
    }

    val startupNode = document.getElementsByTagName("startup").item(0)
    val startupRoute =
      if (startupNode != null && startupNode.nodeType == Node.ELEMENT_NODE) {
        (startupNode as Element).textContent
      } else {
        "default"
      }

    return Pair(startupRoute, routes)
  }
}
