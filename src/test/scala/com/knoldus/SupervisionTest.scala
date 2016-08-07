package com.knoldus
import akka.actor.SupervisorStrategy._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestActors, TestKit, TestProbe}
import akka.util.Timeout
import com.knoldus.Exceptions.StopException
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class SupervisionTest extends TestKit(ActorSystem("supervision-monitoring"))  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "In Supervision and Monitoring test" must {

    "resume handling messages when exception occurs" in {
      val supervisor = TestActorRef[Parent](Props(new Parent()))
      val strategy = supervisor.underlyingActor.supervisorStrategy.decider
      strategy(Exceptions.ResumeException) should be (Resume)
    }

    "stop handling messages when exception occurs" in {
      val supervisor = TestActorRef[Parent](Props(new Parent()))
      val strategy = supervisor.underlyingActor.supervisorStrategy.decider
      strategy(Exceptions.StopException) should be (Stop)
    }

    "restart handling messages when exception occurs" in {
      val supervisor = TestActorRef[Parent](Props(new Parent()))
      val strategy = supervisor.underlyingActor.supervisorStrategy.decider
      strategy(Exceptions.RestartException) should be (Restart)
    }

  }

}
