package v1

import play.api.i18n.Messages

/**
  * Package object for  CarAdvert.  This is a good place to put implicit conversions.
  */
package object caradvert {

  /**
    * Converts between  CarAdvertRequest and Messages automatically.
    */
  implicit def requestToMessages[A](implicit r: CarAdvertRequest[A]): Messages = {
    r.messages
  }
}
