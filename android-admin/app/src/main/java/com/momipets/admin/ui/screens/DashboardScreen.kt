package com.momipets.admin.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momipets.admin.data.*
import com.momipets.admin.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    orders: List<AdminOrder>,
    commissions: List<AdminCommission>,
    payments: List<AdminPayment>,
    notifications: List<AdminNotification>
) {
    val scope = rememberCoroutineScope()
    val pkrFormat = NumberFormat.getNumberInstance(Locale("en", "PK"))

    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val todaysOrders = orders.count { order ->
        val ts = order.createdAt?.toDate()
        ts != null && SimpleDateFormat("yyyy-MM-dd", Locale.US).format(ts) == todayKey
    }
    val pendingCount = orders.count { it.status == "Pending" }
    val deliveredCount = orders.count { it.status == "Delivered" }
    val cancelledCount = orders.count { it.status == "Cancelled" }

    // Never show cancelled/returned revenue as confirmed sales; actual profit comes strictly from commissions ledger
    val totalDeliveredSales = orders
        .filter { it.status == "Delivered" }
        .sumOf { it.totalPkr }
    val confirmedActualProfit = commissions.sumOf { it.actualProfitPkr }
    val totalSupplierPayments = payments.filter { it.status == "Paid" }.sumOf { it.amountPkr }

    val monthlySales = calculateMonthlySales(orders)
    val unreadNotifications = notifications.filter { !it.isRead }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamCanvas)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Business Overview (PKR)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ForestDark
            )
            Text(
                text = "Zero-Inventory Dropshipping · Live Firebase Sync",
                fontSize = 12.sp,
                color = InkMuted
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard("Total Orders", "${orders.size}", Modifier.weight(1f))
                KpiCard("Today's Orders", "$todaysOrders", Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    "Delivered Sales",
                    "PKR ${pkrFormat.format(totalDeliveredSales.toLong())}",
                    Modifier.weight(1f)
                )
                KpiCard(
                    "Confirmed Profit",
                    "PKR ${pkrFormat.format(confirmedActualProfit.toLong())}",
                    Modifier.weight(1f),
                    highlight = true
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard("Pending", "$pendingCount", Modifier.weight(1f))
                KpiCard("Delivered", "$deliveredCount", Modifier.weight(1f))
                KpiCard("Cancelled", "$cancelledCount", Modifier.weight(1f))
            }
        }

        item {
            KpiCard(
                "Paid to Suppliers",
                "PKR ${pkrFormat.format(totalSupplierPayments.toLong())}",
                Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Monthly Delivered Sales (PKR)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ForestDark
                    )
                    MonthlySalesChart(monthlySales)
                }
            }
        }

        item {
            Text(
                text = "Order Alerts (${unreadNotifications.size} Unread)",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ForestDark,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (unreadNotifications.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No unread order alerts.",
                        color = InkMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(unreadNotifications.take(10), key = { it.id }) { notif ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ForestTint),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notif.title, fontWeight = FontWeight.SemiBold, color = ForestDark)
                            Text(notif.message, fontSize = 12.sp, color = InkMuted)
                        }
                        TextButton(onClick = {
                            scope.launch { FirebaseAdminRepository.markNotificationRead(notif.id) }
                        }) {
                            Text("Mark Read", color = ForestDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) ForestDark else CrispWhite
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (highlight) CreamCanvas.copy(alpha = 0.8f) else InkMuted
            )
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) CreamCanvas else ForestDark
            )
        }
    }
}

@Composable
private fun MonthlySalesChart(points: List<Pair<String, Double>>) {
    val maxVal = (points.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val barWidth = size.width / (points.size * 2f).coerceAtLeast(1f)
            points.forEachIndexed { idx, (_, value) ->
                val ratio = (value / maxVal).toFloat()
                val barHeight = size.height * ratio
                val left = idx * barWidth * 2f + barWidth * 0.5f
                drawRect(
                    color = ForestMid,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight.coerceAtLeast(4f))
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { (month, _) ->
                Text(month, fontSize = 10.sp, color = InkMuted)
            }
        }
    }
}

private fun calculateMonthlySales(orders: List<AdminOrder>): List<Pair<String, Double>> {
    val fmt = SimpleDateFormat("MMM", Locale.US)
    val cal = Calendar.getInstance()
    val buckets = LinkedHashMap<String, Double>()
    for (i in 5 downTo 0) {
        val c = Calendar.getInstance()
        c.time = cal.time
        c.add(Calendar.MONTH, -i)
        buckets[fmt.format(c.time)] = 0.0
    }
    orders.filter { it.status == "Delivered" }.forEach { order ->
        val date = order.createdAt?.toDate() ?: return@forEach
        val key = fmt.format(date)
        if (buckets.containsKey(key)) {
            buckets[key] = (buckets[key] ?: 0.0) + order.totalPkr
        }
    }
    return buckets.entries.map { it.key to it.value }
}
