// See README.md for license details.
package sni.netsec.dma

import java.io.PrintWriter
import java.nio.file.{Files, Path, Paths}

import chisel3._
import chisel3.iotesters._
import sni.util._

import scala.collection.immutable

/**
 * Utility object for data packing.
 */
object AlignBufferTestUtil {

  import scala.util.Random

  type StreamData = Seq[(Int, Int, BigInt, BigInt)]

  val r = new Random(1)


  import math.{abs, pow}

  /**
   * IntのデータをSeq[Int]に分解する
   *
   * @param busBits データのビット幅
   * @param data 分解するデータ
   * @param step 何ビット単位で分解するか
   * @return
   */
  def intToSeqInt(busBits: Int, data: BigInt, step: Int = -1): Seq[Int] = {
    val seqData = Range(busBits, 0, step).map {
      i =>
        val mask = pow(2, abs(step)).toInt - 1
        val divData = (data >> (i + step)) & mask
        divData.toInt
    }

    seqData
  }

  /**
   * Making mask value.
   * @param seqOfStrb Strobe data.
   * @return Mask value for data.
   */
  def makeMaskFromSeqInt(seqOfStrb: Seq[Int]): BigInt = {
    val rawDataMaskSeq: Seq[Int] = seqOfStrb.map { i => if (i == 1) { 0xff } else { 0x0 }}

    BigInt(rawDataMaskSeq.foldLeft("")((r, i) => r + f"$i%02x"), 16)
  }

  /**
   * Packing data.
   * @param strb Strobe data for data.
   * @param data Data
   * @return Strobe and data after packing.
   */
  def pack(strb: Seq[Int], data: Seq[Int]): (BigInt, BigInt) = {
    val strbMsb = BigInt(1) << (data.length - 1)
    val bits = data.length * 8

    val r = strb.map(_ == 0x1).zip(data).foldLeft(
      ((BigInt(0), BigInt(0)), 0)) {
      (prev, input) =>
        val (inStrb, inData) = input
        val ((prevStrb, prevData), prevDstBit) = prev

        val nextDstBit = if (inStrb) { prevDstBit + 1 } else prevDstBit
        val strb = if (inStrb) { prevStrb | (strbMsb >> prevDstBit) }
        else        { prevStrb }
        val data = if (inStrb) { prevData | (BigInt(inData) << (bits - 8  - (prevDstBit * 8))) }
        else        { prevData }
        ((strb, data), nextDstBit)
    }

    //println(f"(strb, data) = (0x${r._1._1}%x, 0x${r._1._2}%032x)")

    r._1
  }

  def setRandSeed(seed: Long): Unit = {
    r.setSeed(seed)
  }

  def makeSeqStrb(strbBit: Int): Seq[BigInt] = {
    val mask = (BigInt(1) << strbBit) - 1
    Range(1, strbBit + 1).flatMap {
      i =>
        val base = (BigInt(1) << i) - 1
        Range(0, strbBit + 1).map {
          j => (base << (j - i)) & mask
        }.filter(_ != 0)
      }.distinct
    }

  def makeSeriesStrb(reqLength: Int, preInvBytesRatio: Double, postInvBytesRatio: Double): Seq[Int] = {
    val validStrbRatio = 1 - (preInvBytesRatio + postInvBytesRatio)
    val validStrbLength = math.ceil(reqLength * validStrbRatio).toInt
    val validStrb = Seq.fill(validStrbLength)(1)

    val preInvalidStrbLength = math.ceil(reqLength * preInvBytesRatio).toInt
    val preInvalidStrb = Seq.fill(preInvalidStrbLength)(0)
    val postInvalidStrbLength = math.ceil(reqLength * postInvBytesRatio).toInt
    val postInvalidStrb = Seq.fill(postInvalidStrbLength)(0)

    (preInvalidStrb ++ validStrb ++ postInvalidStrb).slice(0, reqLength)
  }

  def makeRandSeriesStrb(reqLength: Int): Seq[Int] = {

    def getRatio: (Double, Double) = {
      var a = 1.0
      var b = 1.0

      while ((a + b) >= 1.0) {
        a = r.nextDouble()
        b = r.nextDouble()
      }
      (a, b)
    }

    val (preInvBytesRatio, postInvBytesRatio) = getRatio

    makeSeriesStrb(reqLength, preInvBytesRatio, postInvBytesRatio)
  }

  def makeRandStrb(reqLength: Int, invBytesRatio: Double): Seq[Int] = {
    val validStrb = Seq.fill(reqLength)(1)
    val invalidStrb = Seq.fill((reqLength * invBytesRatio).toInt)(0)
    r.shuffle(validStrb ++ invalidStrb)
  }

  def makeRandData(strb: Seq[Int]): Seq[Byte] = {
    val tmp = new Array[Byte](strb.length)
    r.nextBytes(tmp)
    val dst = strb.zip(tmp).map { case (s, d) => d }
    dst
  }

  def makeExpData(strb: Seq[Int], data: Seq[Byte]): Seq[(Int, Byte)] = {
    strb.zip(data).filter(_._1 == 1)
  }

  /**
   * Reshape test data
   * @param numOfBusBytes Number of bus bytes.
   * @param bits Input test data
   * @return The data which added "last" signal information and reshape it.
   */
  def reshapeBits(numOfBusBytes:Int,
                  bits: Seq[(Int, Byte)],
                  randPadding: Boolean = false
                 ): Seq[(Int, Int, BigInt, BigInt)] = {
    val modBusBytes = bits.length % numOfBusBytes
    val lenOfPadded = if (modBusBytes == 0) 0 else numOfBusBytes - modBusBytes
    val padding = if (randPadding) { Seq.fill(lenOfPadded)((0, r.nextInt().toByte)) }
                  else             { Seq.fill(lenOfPadded)((0, 0.toByte))}
    val paddedBits = bits ++ padding

    val reshapedData = Range(0, paddedBits.length, numOfBusBytes).map {
      i =>
        val bits = paddedBits.slice(i, i + numOfBusBytes)
        val reshape = bits.foldLeft((BigInt(0), BigInt(0))) {
          (r, in) => {
            ((r._1 << 1) | BigInt(in._1), (r._2 << 8) | (BigInt(in._2) & 0xff))
          }
        }
        //val last = if ((i / 16) == lastIdx) 1 else 0
        reshape
    }

    val filteredData = reshapedData.filter(_._1 != 0x0)

    val lastIdx = filteredData.length - 1

    filteredData.zipWithIndex.map {
      case (d, i) =>
        val last = if (i == lastIdx) 1 else 0
        (0, last, d._1, d._2)
    }
  }

  /**
   * Output offset input data to file.
   *
   * This method will make files follow:
   *  - rootFileName
   * @param rootFileName File path with file root name
   */
  def writeTestOffsetFile(rootFileName: Path, data: Seq[Int]): Unit = {

    val rootDir = rootFileName.getParent

    println(rootDir.toString)

    if(Files.notExists(rootDir)) Files.createDirectories(rootDir)

    val opw = new PrintWriter(rootFileName.toString)

    // データ数の書き込み
    opw.write(f"${data.length}%x\n")

    // オフセットデータの書き込み
    data.foreach(o => opw.write(f"$o%x\n"))

    opw.close()
  }

  /**
   * Output test input data to file.
   *
   * This method will make files follow:
   *  - rootFileName + "_last"
   *  - rootFileName + "_strb"
   *  - rootFileName + "_data"
   * @param rootFileName File path with file root name
   */
  def writeTestDataFile(rootFileName: Path, data: Seq[(Int, Int, BigInt, BigInt)]): Unit = {

    val rootDir = rootFileName.getParent

    println(rootDir.toString)

    if(Files.notExists(rootDir)) Files.createDirectories(rootDir)

    val lpw = new PrintWriter(rootFileName.toString + "_last")
    val spw = new PrintWriter(rootFileName.toString + "_strb")
    val dpw = new PrintWriter(rootFileName.toString + "_data")
    val epw = new PrintWriter(rootFileName.toString + "_error")

    dpw.write(f"${data.length}%x\n")

    data.foreach {
      case (e, l, s, d) =>
        epw.write(f"$e%d\n")
        lpw.write(f"$l%d\n")
        spw.write(f"$s%x\n")
        dpw.write(f"$d%x\n")
    }

    lpw.close()
    spw.close()
    dpw.close()
    epw.close()
  }
}


/**
 * Unit test class for PacketBuffer
 * @param c AlignBuffer instance
 */
class AlignBufferUnitTester(c: SimDTMAlignBuffer)(implicit isDebug: Boolean = false) extends PeekPokeTester(c) {

  while (peek(c.io.finish) != 0x1) {
    step(1)
  }
  expect(c.io.finish, true)
  expect(c.io.result, false)

  step(10)
}

/**
 * ベースとなるAlignBufferのテストクラス
 */
abstract class BaseAlignBufferTester extends BaseTester {

  import AlignBufferTestUtil._

  val dutName = "AlignBuffer"

  behavior of dutName

  val testArgs = Array(
    s"-tn=$dutName"
  )


  val testRootDir: String
  implicit val isDebug: Boolean

  def getLimit(x: Int): Int

  def runTest(dataBits: Int,
              testDir: Path,
              mode: Boolean,
              strb: Seq[Seq[Int]],
              random: (Boolean, Boolean),
              randSeed: Long = 1
             ): Unit = {

    val testDataFileRoot = Seq("input", "output", "offset").map(s => s -> Paths.get(testDir.toString, s)).toMap

    val data = strb.map(makeRandData)
    val strbBits = dataBits / 8

    val unalignedData = strb.zip(data).flatMap { case (s, d) => reshapeBits(strbBits, s zip d) }
    val alignedData = strb.zip(data).flatMap { case (s, d) => reshapeBits(strbBits, makeExpData(s, d)) }

    // offsetデータの生成
    val offsets = strb.map {
      case seq: immutable.Seq[_] =>
        seq.indexWhere(i => i == 1)
    }

    writeTestOffsetFile(testDataFileRoot("offset"), offsets)

    // 入出力データの書き出し
    if (mode) {
      writeTestDataFile(testDataFileRoot("input"), unalignedData)
      writeTestDataFile(testDataFileRoot("output"), alignedData)
    } else {
      writeTestDataFile(testDataFileRoot("input"), alignedData)
      writeTestDataFile(testDataFileRoot("output"), unalignedData)
    }

    if (isDebug) {
      alignedData.foreach { case (e, l, s, d) => info(f"IN (error, last, strb, data) = ($e, $l, 0x$s%x, 0x$d%x)") }
      unalignedData.foreach { case (e, l, s, d) => info(f"EXP(error, last, strb, data) = ($e, $l, 0x$s%x, 0x$d%x)") }
    }

    iotesters.Driver.execute(
      (testArgs  ++ getArgs) :+ s"-td=$testDir",
      () => new SimDTMAlignBuffer(dataBits, random, randSeed, getLimit(alignedData.length))(testDataFileRoot, alignedData.length)
    ) {
      c => new AlignBufferUnitTester(c)
    } should be (true)
  }
}

sealed trait AlignMode
case object Unalign extends AlignMode { def apply(): Boolean = false }
case object Align extends AlignMode { def apply(): Boolean = true  }

class AlignBufferTester extends BaseAlignBufferTester {

  import AlignBufferTestUtil._

  behavior of dutName

  implicit val isDebug = false

  val testRootDir = s"test_run_dir"
  val random = (false, false)

  def getLimit(x: Int): Int = x * 2

  val dataBitsList = Seq(32, 64, 128, 256)

  /**
   * Test starts here.
   */
  for (dataBits <- dataBitsList) {

    val testBitsDir = s"$testRootDir/$dutName/bits-$dataBits/basic"
    val testRootName = s"$dutName-$dataBits-bits"
    val strbBits = dataBits / 8

    it should s"連続するストローブをMSB側にアラインできる (1byte - 15byte) [$testRootName-0001]" in {

      val testDir = Paths.get(testBitsDir, "0001")

      val strb = makeSeqStrb(strbBits).map(intToSeqInt(strbBits, _))

      runTest(dataBits, testDir, Align(), strb, random)
    }

    it should s"連続するストローブをMSB側にアラインできる (15byte + (1byte - 16byte) [$testRootName-0002]" in {

      val testDir = Paths.get(testBitsDir, "0002")

      val strb = makeSeqStrb(strbBits).map((Seq.fill(strbBits - 1)(0x1) :+ 0x0) ++ intToSeqInt(strbBits, _))

      runTest(dataBits, testDir, Align(), strb, random)
    }

    it should s"連続するストローブをMSB側にアラインできる (16byte + (1byte - 16byte) [$testRootName-0003]" in {

      val testDir = Paths.get(testBitsDir, "0003")

      val strb = makeSeqStrb(strbBits).map(Seq.fill(strbBits - 1)(0x1) ++ intToSeqInt(strbBits, _))

      runTest(dataBits, testDir, Align(), strb, random)
    }

    it should s"連続するストローブをMSB側にアラインできる " +
      s"(1byte - 16 byte + 16byte + (1byte - 16byte) [$testRootName-0004]" in {

      val inStrb = makeSeqStrb(strbBits)
      val testDir = Paths.get(testBitsDir, "0004")

      val lastRange = makeSeqStrb(strbBits)

      val strb = inStrb.flatMap {
        i =>
          lastRange.map(j => intToSeqInt(strbBits, i) ++ Seq.fill(strbBits)(0x1) ++ intToSeqInt(strbBits, j))
      }

      runTest(dataBits, testDir, Align(), strb, random)
    }

    it should s"連続する様々なストローブの組み合わせを処理できる (1byte - 16byte) [$testRootName-0005]" in {

      val testDir = Paths.get(testBitsDir, "0005")

      val strb = Range(0, 4096).map(_ => makeRandSeriesStrb(r.nextInt(strbBits * 2)))

      runTest(dataBits, testDir, Align(), strb, random)
    }

    it should s"連続する様々なストローブの組み合わせを処理できる (1byte - 128byte) [$testRootName-0006]" in {

      val testDir = Paths.get(testBitsDir, "0006")

      val strb = Range(0, 4096).map(_ => makeRandSeriesStrb(r.nextInt(strbBits * 4)))

      runTest(dataBits, testDir, Align(), strb, random)
    }
  }
}

/**
 * Align Buffer Tester (Input Random Test)
 */
class InputRandomAlignBufferTester extends BaseAlignBufferTester {

  import AlignBufferTestUtil._

  behavior of dutName

  val testRootDir = s"test_run_dir/$dutName/input-random"
  val random = (true, false)
  implicit val isDebug = false

  def getLimit(x: Int): Int = x * 100

  /**
   * Test starts here.
   */
  for (dataBits <- 32 to 32 by 32) {
    it should s"連続するストローブをMSB側にアラインできる " +
      s"(1byte - 15byte) [$dutName-0001]" in {

      for (i <- 0 until 10) {
        val simRandSeed = (i + r.nextLong()) & 0x7fffffffffffffffL
        info(f"Sim Random Seed = 0x$simRandSeed%016x")

        val testDir = Paths.get(testRootDir, "0001")
        val strb = makeSeqStrb(16).map(intToSeqInt(16, _))

        runTest(dataBits, testDir, Align(), strb, random, simRandSeed)
      }
    }

    it should s"連続するストローブをMSB側にアラインできる " +
      s"(15byte + (1byte - 16byte) [$dutName-0002]" in {

      for (i <- 0 until 10) {
        val simRandSeed = (i + r.nextLong()) & 0x7fffffffffffffffL
        info(f"Sim Random Seed = 0x$simRandSeed%016x")

        val testDir = Paths.get(testRootDir, "0002")
        val strb = makeSeqStrb(16).map((Seq.fill(15)(0x1) :+ 0x0) ++ intToSeqInt(16, _))

        runTest(dataBits, testDir, Align(), strb, random, simRandSeed)
      }
    }

    it should s"be able to pack data which all strobe combinations. " +
      s"(16byte + (1byte - 16byte) [$dutName-0003]" in {

      for (i <- 0 until 10) {
        val simRandSeed = (i + r.nextLong()) & 0x7fffffffffffffffL
        info(f"Sim Random Seed = 0x$simRandSeed%016x")

        val testDir = Paths.get(testRootDir, "0003")
        val strb = makeSeqStrb(16).map(Seq.fill(16)(0x1) ++ intToSeqInt(16, _))

        runTest(dataBits, testDir, Align(), strb, random, simRandSeed)
      }
    }

    it should s"連続するストローブをMSB側にアラインできる " +
      s"(1byte - 16 byte + 16byte + (1byte - 16byte) [$dutName-0004]" in {

      val inStrb = makeSeqStrb(16)

      for (i <- 0 until 10) {
        val simRandSeed = (i + r.nextLong()) & 0x7fffffffffffffffL
        info(f"Sim Random Seed = 0x$simRandSeed%016x")
        val testDir = Paths.get(testRootDir, "0004")

        val lastRange = makeSeqStrb(16)
        val strb = inStrb.flatMap {
          i =>
            lastRange.map(j => intToSeqInt(16, i) ++ Seq.fill(16)(0x1) ++ intToSeqInt(16, j))
        }

        runTest(dataBits, testDir, Align(), strb, random, simRandSeed)
      }
    }

    it should s"連続する様々なストローブの組み合わせを処理できる (1byte - 32byte) [$dutName-0005]" in {

      val testDir = Paths.get(testRootDir, "0005")

      for (i <- 0 until 10) {
        val simRandSeed = (i + r.nextLong()) & 0x7fffffffffffffffL
        info(f"Sim Random Seed = 0x$simRandSeed%016x")
        val strb = Range(0, 4096).map(_ => makeRandSeriesStrb(r.nextInt(32)))

        runTest(dataBits, testDir, Align(), strb, random, simRandSeed)
      }
    }

    it should s"連続する様々なストローブの組み合わせを処理できる (1byte - 128byte) [$dutName-0006]" in {

      val testDir = Paths.get(testRootDir, "0006")

      for (i <- 0 until 10) {
        val simRandSeed = (i + r.nextLong()) & 0x7fffffffffffffffL
        info(f"Sim Random Seed = 0x$simRandSeed%016x")
        val strb = Range(0, 4096).map(_ => makeRandSeriesStrb(r.nextInt(128)))

        runTest(dataBits, testDir, Align(), strb, random, simRandSeed)
      }
    }
  }
}

/**
 * Align Buffer Tester (Output Random Test)
 */
class OutputRandomAlignBufferTester extends InputRandomAlignBufferTester {

  behavior of dutName

  override val testRootDir = s"test_run_dir/$dutName/output-random"
  override val random = (false, true)

  override implicit val isDebug = false
}

/**
 * Align Buffer Tester (Inout Random Test)
 */
class InOutRandomAlignBufferTester extends InputRandomAlignBufferTester {

  behavior of dutName

  override val testRootDir = s"test_run_dir/$dutName/inout-random"
  override val random = (true, true)

  override implicit val isDebug = false

  override def getLimit(x: Int): Int = x * 100
}
