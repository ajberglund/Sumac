package com.quantifind.sumac

import annotation.tailrec
import java.lang.reflect.{Type, ParameterizedType, Field}

object ReflectionUtils {

  @tailrec
  def getAllDeclaredFields(cls: Class[_], acc: Seq[Field] = Seq.empty): Seq[Field] = {
    val fields = acc ++ cls.getDeclaredFields
    Option(cls.getSuperclass) match {
      case Some(clazz) => getAllDeclaredFields(clazz, fields)
      case _ => fields
    }
  }

  def findGenericInterface(cls: Class[_], interface: Class[_]) : Option[ParameterizedType] = {
    val interfaces = cls.getGenericInterfaces
    //first check if this directly implements it
    findParameterizedType(interfaces, interface).orElse{
      //then check if its inherited by any of those interfaces
      interfaces.flatMap{tpe =>
        findGenericInterface(getRawClass(tpe), interface)
      }.headOption
    }
  }

  def getRawClass(tpe: Type) : Class[_] = {
    tpe match {
      case x: Class[_] => x
      case p: ParameterizedType => p.getRawType.asInstanceOf[Class[_]]
    }
  }

  def findParameterizedType(types: Array[Type], interface: Class[_]) : Option[ParameterizedType] = {
    types.find { tpe =>
      tpe match {
        case ptpe: ParameterizedType => ParseHelper.checkType(ptpe, classOf[Argable[_]])
        case _ => false
      }
    }.map{_.asInstanceOf[ParameterizedType]}
  }
}
