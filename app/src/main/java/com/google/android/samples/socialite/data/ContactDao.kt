/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.socialite.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.google.android.samples.socialite.model.Contact
import com.google.android.samples.socialite.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT COUNT(id) FROM Contact")
    suspend fun count(): Int

    @Insert
    suspend fun insert(contact: Contact)
    
    @Update
    suspend fun updateContact(contact: Contact)

    @Query("SELECT * FROM Contact")
    suspend fun loadAll(): List<Contact>
    
    @Query("SELECT * FROM Contact WHERE id = :contactId")
    suspend fun getContactById(contactId: Long): Contact?
    
    /**
     * Get all contacts with a specific role
     */
    @Query("SELECT * FROM Contact WHERE role = :role")
    suspend fun getContactsByRole(role: Int): List<Contact>
    
    /**
     * Get all band members
     */
    @Query("SELECT * FROM Contact WHERE role = :bandMemberRole")
    suspend fun getAllBandMembers(bandMemberRole: Int = UserRole.BAND_MEMBER.ordinal): List<Contact>
    
    /**
     * Get all fans
     */
    @Query("SELECT * FROM Contact WHERE role = :fanRole")
    suspend fun getAllFans(fanRole: Int = UserRole.FAN.ordinal): List<Contact>
    
    /**
     * Get all contacts that have upcoming concerts
     */
    @Query("SELECT * FROM Contact WHERE upcomingConcert != ''")
    suspend fun getContactsWithUpcomingConcerts(): List<Contact>
    
    /**
     * Get all contacts with a specific genre
     */
    @Query("SELECT * FROM Contact WHERE genre LIKE '%' || :genreQuery || '%'")
    suspend fun getContactsByGenre(genreQuery: String): List<Contact>
    
    /**
     * Search for contacts by name or band name
     */
    @Query("SELECT * FROM Contact WHERE name LIKE '%' || :query || '%' OR bandName LIKE '%' || :query || '%'")
    fun searchContacts(query: String): Flow<List<Contact>>
}