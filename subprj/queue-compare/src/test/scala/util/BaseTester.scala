// See README.md for license details.

package sni.util

import scala.util.Random

import chisel3.iotesters._
import org.scalatest.{BeforeAndAfterAllConfigMap, ConfigMap}

/**
 * Base Tester class
 */
abstract class BaseTester extends ChiselFlatSpec with BeforeAndAfterAllConfigMap  {

  val defaultArgs = scala.collection.mutable.Map(
    "-tgvo" -> "on",
    "-tbn" -> "verilator",
    "-tiv" -> false
  )

  var numOfLoop = 1

  var r = new Random(1)

  /**
   * Parse and set Chisel tester arguments.
   * @param configMap ConfigMap instance from FlatSpec.
   */
  override def beforeAll(configMap: ConfigMap): Unit = {
    def optIsDefined(optName: String): Boolean = configMap.get(optName).isDefined

    // choose simulation backend
    if (optIsDefined("--backend-name")) {
      defaultArgs("-tbn") = configMap.get("--backend-name").fold("")(_.toString)
    } else if (optIsDefined("-tbn")) {
      defaultArgs("-tbn") = configMap.get("-tbn").fold("")(_.toString)
    }

    // choose wave dump or not
    if (optIsDefined("--generate-vcd-output")) {
      defaultArgs("-tgvo") = configMap.get("--generate-vcd-output").fold("")(_.toString)
    } else if (optIsDefined("-tgvo")) {
      defaultArgs("-tgvo") = configMap.get("-tgvo").fold("")(_.toString)
    }

    // choose log verbosity
    if (optIsDefined("--is-verbose")) {
      defaultArgs("-tiv") = true
    } else if (optIsDefined("-tiv")) {
      defaultArgs("-tiv") = true
    }

    // choose how many loops on test
    if (configMap.get("--loop-num").isDefined) {
      numOfLoop = configMap.get("--loop-num").last.toString.toInt
    }

    // choose how to treat random generator seed
    if (configMap.get("--rand-seed").isDefined) {
      val randSeed = configMap.get("--rand-seed").last.toString match {
        case str if str == "systime" => System.currentTimeMillis()
        case str => try {
          str.toInt
        } catch {
          case _: java.lang.NumberFormatException => 1
        }
      }
      println(f"random seed = 0x$randSeed%x")
      r = new Random(randSeed)
    }
  }

  /**
   * Get argumentation Array from configMap.
   * @return Array of option strings.
   */
  def getArgs: Array[String] = {
    val argsMap = defaultArgs
    argsMap.map {
      case (key: String, value: String) => s"$key=$value"
      case (key: String, value: Boolean) => if (value) key else ""
    }.toArray
  }

  def dutName: String
}
