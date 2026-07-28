package com.thanh0x.coursedeal.repository

import com.thanh0x.coursedeal.model.user.UserPreference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserPreferenceRepository : JpaRepository<UserPreference, Int>
