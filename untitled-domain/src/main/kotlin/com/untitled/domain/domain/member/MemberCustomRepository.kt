package com.untitled.domain.domain.member

interface MemberCustomRepository {

    fun findByName(name: String): MemberEntity?

}
