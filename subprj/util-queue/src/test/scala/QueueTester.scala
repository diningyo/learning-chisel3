
import scala.util.Random

import chisel3._
import chisel3.util._
import chisel3.iotesters._

class QueueTester extends ChiselFlatSpec {

  def dutName: String = "Queue"

  behavior of dutName

  val numOfEntry = 8
  val r = new Random(1)

  it should "PipeOffFlowOff" in {
    val cfg = "PipeOffFlowOff"
    iotesters.Driver.execute(Array(
      s"-tn=$dutName$cfg",
      s"-td=test_run_dir/$dutName$cfg",
      "-tgvo=on",
      "-tbn=treadle"
    ),
      () => new Queue(UInt(8.W), numOfEntry, pipe = false, flow = false)) {
      c => new PeekPokeTester(c) {

        // １．対向がデータを受け取れる時(deq.ready == true)
        poke(c.io.enq.valid, false)
        poke(c.io.deq.ready, true)

        for (_ <- 0 until numOfEntry) {
          val data = r.nextInt(0xff)
          poke(c.io.enq.valid, true)
          poke(c.io.enq.bits, data)
          step(1)
        }

        poke(c.io.enq.valid, false)
        step(5)

        // ２．対向がデータを受け取れない時(deq.ready == false)
        poke(c.io.enq.valid, false)
        poke(c.io.deq.ready, false)

        for (_ <- 0 until numOfEntry) {
          val data = r.nextInt(0xff)
          poke(c.io.enq.valid, true)
          poke(c.io.enq.bits, data)
          step(1)
        }

        for (_ <- 0 until numOfEntry) {
          poke(c.io.enq.valid, false)
          poke(c.io.deq.ready, true)
          step(1)
        }

        step(5)
      }
    }
  }

  it should "PipeOnFlowOff" in {
    val cfg = "PipeOnFlowOff"
    iotesters.Driver.execute(Array(
      s"-tn=$dutName$cfg",
      s"-td=test_run_dir/$dutName$cfg",
      "-tgvo=on",
      "-tbn=treadle"
    ),
      () => new Queue(UInt(8.W), numOfEntry, pipe = true, flow = false)) {
      c => new PeekPokeTester(c) {
        // １．対向がデータを受け取れる時(deq.ready == true)
        poke(c.io.enq.valid, false)
        poke(c.io.deq.ready, true)

        for (_ <- 0 until numOfEntry) {
          val data = r.nextInt(0xff)
          poke(c.io.enq.valid, true)
          poke(c.io.enq.bits, data)
          step(1)
        }

        poke(c.io.enq.valid, false)
        step(5)

        // ２．対向がデータを受け取れない時(deq.ready == false)
        poke(c.io.enq.valid, false)
        poke(c.io.deq.ready, false)

        for (_ <- 0 until numOfEntry) {
          val data = r.nextInt(0xff)
          poke(c.io.enq.valid, true)
          poke(c.io.enq.bits, data)
          step(1)
        }

        for (_ <- 0 until numOfEntry) {
          poke(c.io.enq.valid, false)
          poke(c.io.deq.ready, true)
          step(1)
        }

        step(5)
      }
    }
  }

  it should "PipeOffFlowOn" in {
    val cfg = "PipeOffFlowOn"
    iotesters.Driver.execute(Array(
      s"-tn=$dutName$cfg",
      s"-td=test_run_dir/$dutName$cfg",
      "-tgvo=on",
      "-tbn=treadle"
    ),
      () => new Queue(UInt(8.W), numOfEntry, pipe = false, flow = true)) {
      c => new PeekPokeTester(c) {
        // １．対向がデータを受け取れる時(deq.ready == true)
        poke(c.io.enq.valid, false)
        poke(c.io.deq.ready, true)

        for (_ <- 0 until numOfEntry) {
          val data = r.nextInt(0xff)
          poke(c.io.enq.valid, true)
          poke(c.io.enq.bits, data)
          step(1)
        }

        poke(c.io.enq.valid, false)
        step(5)

        // ２．対向がデータを受け取れない時(deq.ready == false)
        poke(c.io.enq.valid, false)
        poke(c.io.deq.ready, false)

        for (_ <- 0 until numOfEntry) {
          val data = r.nextInt(0xff)
          poke(c.io.enq.valid, true)
          poke(c.io.enq.bits, data)
          step(1)
        }

        for (_ <- 0 until numOfEntry) {
          poke(c.io.enq.valid, false)
          poke(c.io.deq.ready, true)
          step(1)
        }

        step(5)
      }
    }
  }

  it should "PipeOnFlowOn" in {
    val cfg = "PipeOnFlowOn"
    iotesters.Driver.execute(Array(
      s"-tn=$dutName$cfg",
      s"-td=test_run_dir/$dutName$cfg",
      "-tgvo=on",
      "-tbn=treadle"
    ),
      () => new Queue(UInt(8.W), numOfEntry, pipe = true, flow = true)) {
      c => new PeekPokeTester(c) {
        // １．対向がデータを受け取れる時(deq.ready == true)
        poke(c.io.enq.valid, false)
        poke(c.io.deq.ready, true)

        for (_ <- 0 until numOfEntry) {
          val data = r.nextInt(0xff)
          poke(c.io.enq.valid, true)
          poke(c.io.enq.bits, data)
          step(1)
        }

        poke(c.io.enq.valid, false)
        step(5)

        // ２．対向がデータを受け取れない時(deq.ready == false)
        poke(c.io.enq.valid, false)
        poke(c.io.deq.ready, false)

        for (_ <- 0 until numOfEntry) {
          val data = r.nextInt(0xff)
          poke(c.io.enq.valid, true)
          poke(c.io.enq.bits, data)
          step(1)
        }

        for (_ <- 0 until numOfEntry) {
          poke(c.io.enq.valid, false)
          poke(c.io.deq.ready, true)
          step(1)
        }

        step(5)
      }
    }
  }
}
