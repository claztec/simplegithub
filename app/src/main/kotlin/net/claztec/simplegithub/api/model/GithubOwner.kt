package net.claztec.simplegithub.api.model

import com.google.gson.annotations.SerializedName

class GithubOwner(val login: String, @field:SerializedName("avatar_url")
val avatarUrl: String)
