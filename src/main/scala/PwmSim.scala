// package com.flogics.spinal.pwmled

import spinal.sim._
import spinal.core.sim._

object PwmSim {
  def main(args: Array[String]): Unit = {
    SimConfig.withWave.compile(new Pwm(size = 4)).doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)
      dut.io.width #= 5
      for (i <- 0 until 100) {
        dut.clockDomain.waitSampling()
      }
    }
  }
}
