package com.momipets.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momipets.admin.data.*
import com.momipets.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WebsiteSettingsScreen(
    settings: AdminWebsiteSettings,
    banners: List<AdminBanner>
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var storeName by remember(settings) { mutableStateOf(settings.storeName) }
    var ownerName by remember(settings) { mutableStateOf(settings.ownerName) }
    var businessLocation by remember(settings) { mutableStateOf(settings.businessLocation) }
    var tagline by remember(settings) { mutableStateOf(settings.tagline) }
    var logoUrl by remember(settings) { mutableStateOf(settings.logoUrl) }
    var whatsappDisplay by remember(settings) { mutableStateOf(settings.whatsappDisplay) }
    var whatsapp by remember(settings) { mutableStateOf(settings.whatsappNumber) }
    var email by remember(settings) { mutableStateOf(settings.supportEmail) }
    var announcement by remember(settings) { mutableStateOf(settings.announcementText) }
    var shippingPkr by remember(settings) { mutableStateOf(settings.standardShippingPkr.toString()) }
    var freeThreshold by remember(settings) { mutableStateOf(settings.freeDeliveryThresholdPkr.toString()) }
    var codNote by remember(settings) { mutableStateOf(settings.codPolicyNote) }

    val mainBanner = banners.firstOrNull() ?: AdminBanner(
        id = "banner_hero_main",
        title = "Wholesome nutrition & calm daily care for your companions.",
        subtitle = "Curated cat, dog, bird, aquarium, and small-pet essentials fulfilled directly from verified Pakistani suppliers.",
        ctaLabel = "Explore Catalog",
        imageUrl = "/src/assets/images/hero_pets_store_1795415307566.jpg"
    )
    var bannerTitle by remember(banners) { mutableStateOf(mainBanner.title) }
    var bannerSubtitle by remember(banners) { mutableStateOf(mainBanner.subtitle) }
    var bannerImage by remember(banners) { mutableStateOf(mainBanner.imageUrl) }

    var feedback by remember { mutableStateOf<String?>(null) }

    val waDigits = whatsapp.replace(Regex("[^0-9]"), "").let {
        if (it.startsWith("0")) "92${it.drop(1)}" else it
    }.ifBlank { "923363958385" }
    val waUrl = "https://wa.me/$waDigits"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamCanvas)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Website & Owner Contact Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ForestDark)
        Text("Editable only by the authorized admin. Changes sync live to MOMI PETS STORE.", fontSize = 12.sp, color = InkMuted)

        OutlinedButton(
            onClick = { uriHandler.openUri(waUrl) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Official WhatsApp Support ($whatsappDisplay)", color = ForestDark)
        }

        OutlinedButton(
            onClick = {
                scope.launch {
                    FirebaseAdminRepository.seedDefaultCategoriesAndSettings()
                    feedback = "53 pet categories & default store settings initialized in Firestore."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Initialize 53 Default Pet Categories & Store Settings", color = ForestDark)
        }

        if (feedback != null) {
            Text(feedback!!, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ForestDark)
        }

        OutlinedTextField(value = storeName, onValueChange = { storeName = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Owner Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = businessLocation, onValueChange = { businessLocation = it }, label = { Text("Business Location") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = tagline, onValueChange = { tagline = it }, label = { Text("Tagline") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text("Logo URL (Optional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = whatsappDisplay, onValueChange = { whatsappDisplay = it }, label = { Text("Official WhatsApp (Local, e.g. 03363958385)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("International WhatsApp (e.g. +923363958385)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Support Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = announcement, onValueChange = { announcement = it }, label = { Text("Top Announcement Bar") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = shippingPkr, onValueChange = { shippingPkr = it }, label = { Text("Standard Shipping (PKR)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = freeThreshold, onValueChange = { freeThreshold = it }, label = { Text("Free Delivery Threshold (PKR)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = codNote, onValueChange = { codNote = it }, label = { Text("COD Policy Note") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                scope.launch {
                    FirebaseAdminRepository.saveWebsiteSettings(
                        AdminWebsiteSettings(
                            storeName = storeName,
                            ownerName = ownerName,
                            businessLocation = businessLocation,
                            tagline = tagline,
                            logoUrl = logoUrl,
                            whatsappDisplay = whatsappDisplay,
                            whatsappNumber = whatsapp,
                            supportEmail = email,
                            announcementText = announcement,
                            standardShippingPkr = shippingPkr.toDoubleOrNull() ?: 350.0,
                            freeDeliveryThresholdPkr = freeThreshold.toDoubleOrNull() ?: 5000.0,
                            codPolicyNote = codNote
                        )
                    )
                    feedback = "Website & owner contact settings saved & published."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Website Settings")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Hero Banner", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ForestDark)
        OutlinedTextField(value = bannerTitle, onValueChange = { bannerTitle = it }, label = { Text("Hero Headline") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = bannerSubtitle, onValueChange = { bannerSubtitle = it }, label = { Text("Hero Subtitle") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = bannerImage, onValueChange = { bannerImage = it }, label = { Text("Hero Image URL") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                scope.launch {
                    FirebaseAdminRepository.saveBanner(
                        mainBanner.copy(
                            title = bannerTitle,
                            subtitle = bannerSubtitle,
                            imageUrl = bannerImage,
                            isActive = true
                        )
                    )
                    feedback = "Hero banner updated on website."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = ForestMid),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Homepage Banner")
        }
    }
}
