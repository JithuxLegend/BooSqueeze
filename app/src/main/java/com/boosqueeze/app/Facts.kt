package com.boosqueeze.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Fact(val topic: String, val text: String)

val FACTS = listOf(
    Fact("🪐 Space", "A day on Venus is longer than its year. It spins so slowly that one turn takes about 243 Earth days, while its trip around the Sun takes about 225."),
    Fact("🪐 Space", "Sunlight takes about 8 minutes and 20 seconds to reach Earth. You always see the Sun as it was a little while ago."),
    Fact("🪐 Space", "Neptune was found with mathematics before anyone saw it. Astronomers predicted where it had to be, then pointed their telescopes there in 1846."),
    Fact("🪐 Space", "Footprints on the Moon can last for millions of years, because there is no wind or water there to wash them away."),
    Fact("🪐 Space", "Saturn is so light for its size that it would float in water, if you could find a bathtub big enough."),
    Fact("🪐 Space", "More than a million Earths could fit inside the Sun."),
    Fact("🪐 Space", "Astronauts get a little taller in space, because their spines stretch when gravity is not pressing down."),
    Fact("🪐 Space", "Jupiter's Great Red Spot is a storm that people have watched through telescopes for well over 150 years."),
    Fact("🐾 Animals", "An octopus has three hearts, and its blood is blue."),
    Fact("🐾 Animals", "A group of flamingos is called a flamboyance."),
    Fact("🐾 Animals", "Elephants can recognise themselves in a mirror, something only a few animals can do."),
    Fact("🐾 Animals", "Crows can recognise and remember individual human faces."),
    Fact("🐾 Animals", "Sea otters sometimes hold hands while they sleep, so they do not drift apart."),
    Fact("🐾 Animals", "A tiger's stripes are not just in its fur. Its skin is striped too."),
    Fact("🐾 Animals", "A blue whale's tongue can weigh as much as an elephant."),
    Fact("🐾 Animals", "Cows form close friendships and can get stressed when they are separated from their friends."),
    Fact("🌍 World", "Russia spans 11 time zones, so it can be breakfast time at one end of the country while it is already evening at the other."),
    Fact("🌍 World", "Canada has the longest coastline of any country in the world."),
    Fact("🌍 World", "The largest desert on Earth is not the Sahara. It is Antarctica, because a desert is defined by how little rain or snow falls."),
    Fact("🌍 World", "Vatican City is the smallest country in the world, with an area of less than half a square kilometre."),
    Fact("🌍 World", "India's constitution recognises 22 official languages, and people speak hundreds more across the country."),
    Fact("🌍 World", "Nepal has the only national flag in the world that is not a rectangle or a square."),
    Fact("🌍 World", "The Pacific Ocean covers more area than all of the world's land put together."),
    Fact("🌍 World", "The Himalayas are still rising by a few millimetres every year, because the Indian plate keeps pushing into Asia."),
    Fact("📚 Literature", "Don Quixote, written by Miguel de Cervantes in the early 1600s, is often called the first modern novel."),
    Fact("📚 Literature", "Mary Shelley started writing Frankenstein when she was only 18 years old."),
    Fact("📚 Literature", "William Shakespeare is credited with the first written use of many English words, including eyeball and bedroom."),
    Fact("📚 Literature", "The Mahabharata is one of the longest poems ever written, roughly ten times longer than the Iliad and Odyssey put together."),
    Fact("📚 Literature", "Rabindranath Tagore became the first non-European to win the Nobel Prize in Literature, in 1913."),
    Fact("📚 Literature", "The first Harry Potter book was turned down by about a dozen publishers before one said yes."),
    Fact("📚 Literature", "Homer's Iliad and Odyssey were made to be spoken aloud, and storytellers passed them on long before they were written down."),
    Fact("📚 Literature", "Charles Dickens published many of his famous novels in weekly or monthly parts, so readers waited eagerly for each new chapter."),
    Fact("🏛️ Stoic wisdom", "Marcus Aurelius, the Roman emperor, wrote Meditations as private notes to himself. He never meant them to be published."),
    Fact("🏛️ Stoic wisdom", "The Stoic teacher Epictetus was born a slave, yet his ideas about inner freedom are still read today."),
    Fact("🏛️ Stoic wisdom", "Stoicism is named after the Stoa, a painted porch in Athens where its founder taught."),
    Fact("🏛️ Stoic wisdom", "Seneca, a famous Stoic writer, was once the tutor and advisor of the emperor Nero."),
    Fact("🏛️ Stoic wisdom", "A core Stoic idea: some things are up to us, like our choices and attitudes, and many are not, like other people's opinions or the weather."),
    Fact("🏛️ Stoic wisdom", "According to tradition, Zeno started Stoicism around 300 BCE after losing his cargo in a shipwreck and finding philosophy in Athens."),
    Fact("🏛️ Stoic wisdom", "Marcus Aurelius ruled Rome, but he wrote his Meditations in Greek, the language of philosophy at the time."),
    Fact("🏛️ Stoic wisdom", "Stoics reminded themselves that life is short, not to feel gloomy, but to value each day more."),
    Fact("🏏 Cricket", "Sachin Tendulkar is the only player to score 100 international centuries, 51 in Tests and 49 in ODIs."),
    Fact("🏏 Cricket", "The Ashes series began in 1882, after a newspaper joked that English cricket had died and its ashes would be taken to Australia."),
    Fact("🏏 Cricket", "India won its first Cricket World Cup in 1983, beating the mighty West Indies in the final at Lord's."),
    Fact("🏏 Cricket", "The first official Test match was played in Melbourne in 1877, between Australia and England."),
    Fact("🏏 Cricket", "A traditional cricket ball has a core of cork wrapped in string, covered in leather."),
    Fact("🏏 Cricket", "A Test match can last five days and still end in a draw."),
    Fact("🏏 Cricket", "India won the very first T20 World Cup in 2007, beating Pakistan in the final."),
    Fact("🏏 Cricket", "Don Bradman's Test batting average of 99.94 is the highest in history for a long career. He needed just four more runs in his last innings to average 100."),
    Fact("⚽ Soccer", "The first FIFA World Cup was held in 1930 in Uruguay, and the host nation won it."),
    Fact("⚽ Soccer", "Pelé is the only player to have won three World Cups, in 1958, 1962 and 1970."),
    Fact("⚽ Soccer", "Goal nets were invented by John Alexander Brodie, an engineer from Liverpool, in the late 1800s."),
    Fact("⚽ Soccer", "The word soccer started as British slang for association football, long before it became popular in America."),
    Fact("⚽ Soccer", "Yellow and red cards were first used at the 1970 World Cup."),
    Fact("⚽ Soccer", "The World Cup trophy is made of solid 18-carat gold and weighs more than 6 kilograms."),
    Fact("⚽ Soccer", "The first international match was played in 1872, when Scotland and England drew 0-0."),
    Fact("⚽ Soccer", "The first Women's World Cup was played in 1991 in China, and the United States won it."),
    Fact("🔬 Science", "Honey almost never spoils, because it has very little water and is naturally acidic."),
    Fact("🔬 Science", "Bananas are very slightly radioactive because they contain potassium, but they are completely safe to eat."),
    Fact("🔬 Science", "A bolt of lightning can be several times hotter than the surface of the Sun."),
    Fact("🔬 Science", "If you unwound the DNA from a single human cell, it would be about two metres long."),
    Fact("🔬 Science", "At a special point called the triple point, water can be ice, liquid and vapour all at once."),
    Fact("🔬 Science", "A teaspoon of neutron star material would weigh billions of tonnes."),
    Fact("🔬 Science", "Sound cannot travel through space, because there is no air to carry it."),
    Fact("🔬 Science", "Your brain is about 2 percent of your body weight, but it uses around 20 percent of your energy.")
)

fun nextFact(current: Int): Int {
    var n = FACTS.indices.random()
    if (n == current && FACTS.size > 1) n = (current + 1) % FACTS.size
    return n
}

@Composable
fun FactCard(fact: Fact, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2A2266))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(20.dp)
    ) {
        Text(
            "💡 Did you know?  ·  " + fact.topic,
            color = Color(0xFF7ED957), fontSize = 13.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Text(fact.text, color = Color.White, fontSize = 16.sp, lineHeight = 23.sp)
        if (onClick != null) {
            Spacer(Modifier.height(10.dp))
            Text("Tap for another fact", color = Color(0xFFB9B2F5), fontSize = 12.sp)
        }
    }
}
