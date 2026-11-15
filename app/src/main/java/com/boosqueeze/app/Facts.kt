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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class Fact(
    val topic: String,
    val text: String,
    val answer: String = "",
    val header: String = "💡 Did you know?"
)

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
    Fact("🔬 Science", "Your brain is about 2 percent of your body weight, but it uses around 20 percent of your energy."),
    Fact("🏺 History", "Cleopatra lived closer in time to the Moon landing than to the building of the Great Pyramid of Giza."),
    Fact("🏺 History", "The Great Wall of China was not built in one go. Different dynasties built and rebuilt it over many centuries."),
    Fact("🏺 History", "The Indus Valley civilisation had planned cities with drainage systems more than 4,000 years ago."),
    Fact("🏺 History", "India became independent from British rule on 15 August 1947."),
    Fact("🏺 History", "Oxford University is older than the Aztec Empire. Teaching there began around 1096, while the Aztec city of Tenochtitlan was founded in 1325."),
    Fact("🏺 History", "After the bloody Kalinga war, the emperor Ashoka turned toward peace and helped spread Buddhist teachings across Asia."),
    Fact("🏺 History", "Johannes Gutenberg's printing press, developed in the 1400s, made books far cheaper and helped ideas spread across Europe."),
    Fact("🏺 History", "The Library of Alexandria was one of the biggest and most famous libraries of the ancient world."),
    Fact("💻 Technology", "The first computer mouse, built in the 1960s, was made of wood."),
    Fact("💻 Technology", "The first message sent over the early internet in 1969 was meant to be the word login, but the system crashed after the first two letters."),
    Fact("💻 Technology", "The first website went live in 1991, and it was about the World Wide Web project itself."),
    Fact("💻 Technology", "The word robot comes from a Czech word meaning forced labour. It was first used in a 1920 play."),
    Fact("💻 Technology", "Ada Lovelace wrote what is often called the first computer program in the 1840s, for a machine that had not even been built yet."),
    Fact("💻 Technology", "The QWERTY keyboard layout goes back to typewriters of the 1800s."),
    Fact("💻 Technology", "A modern smartphone is far more powerful than the computers that helped guide the Apollo missions to the Moon."),
    Fact("💻 Technology", "The @ symbol was first used in an email address in 1971 by the engineer Ray Tomlinson."),
    Fact("🎬 Movies", "India's first full-length feature film, Raja Harishchandra, was released in 1913. It was a silent film made by Dadasaheb Phalke."),
    Fact("🎬 Movies", "Mother India, released in 1957, was the first Indian film nominated for an Academy Award in the foreign language category."),
    Fact("🎬 Movies", "The name Bollywood mixes Bombay, the old name of Mumbai, with Hollywood."),
    Fact("🎬 Movies", "The Lumiere brothers held one of the first public paid film screenings in Paris in 1895."),
    Fact("🎬 Movies", "The mechanical shark in Jaws often broke down, so the director showed it less, and that made the film scarier."),
    Fact("🎬 Movies", "Snow White and the Seven Dwarfs, from 1937, is considered the first full-length hand-drawn animated feature film."),
    Fact("🎬 Movies", "The three Lord of the Rings films were shot together in New Zealand over more than a year."),
    Fact("🎬 Movies", "The Indian director Satyajit Ray received an honorary Academy Award in 1992 for his lifetime of work."),
    Fact("🍜 Food", "Ketchup began as a fish sauce from Southeast Asia, long before anyone added tomatoes."),
    Fact("🍜 Food", "Carrots were not always orange. Early farmed carrots were often purple or yellow."),
    Fact("🍜 Food", "Chillies came to India with Portuguese traders in the 1500s. Before that, Indian food got its heat from black pepper."),
    Fact("🍜 Food", "Pizza Margherita is said to be named after Queen Margherita of Italy, with toppings in the colours of the Italian flag."),
    Fact("🍜 Food", "The Maya and the Aztecs drank chocolate as a bitter drink long before anyone made it sweet."),
    Fact("🍜 Food", "Rice is a main food for about half of the people in the world."),
    Fact("🍜 Food", "Much of the wasabi served outside Japan is really horseradish coloured green."),
    Fact("🍜 Food", "The samosa travelled to India from Central Asia several centuries ago and became one of its favourite snacks."),
    Fact("🧠 Body & mind", "Your heart beats about 100,000 times every day."),
    Fact("🧠 Body & mind", "Babies are born with around 300 bones, but adults have 206, because some bones join together as we grow."),
    Fact("🧠 Body & mind", "Your brain has no pain receptors of its own, even though it is the part that feels pain from everywhere else."),
    Fact("🧠 Body & mind", "Yawning is contagious. Even reading about it can make you yawn."),
    Fact("🧠 Body & mind", "Smell is closely linked to memory, which is why a scent can suddenly bring back a moment from years ago."),
    Fact("🧠 Body & mind", "About 60 percent of the adult human body is water."),
    Fact("🧠 Body & mind", "A few slow, deep breaths can help calm your body when you feel stressed."),
    Fact("🧠 Body & mind", "Even identical twins have different fingerprints."),
    Fact("⚽ Messi", "Lionel Messi was born in Rosario, Argentina, on 24 June 1987."),
    Fact("⚽ Messi", "As a child, Messi needed treatment for a growth hormone problem, and Barcelona offered to pay for it when he was 13."),
    Fact("⚽ Messi", "Messi's first contract with Barcelona is said to have been written on a paper napkin in the year 2000."),
    Fact("⚽ Messi", "Messi has won the Ballon d'Or eight times, more than any other player."),
    Fact("⚽ Messi", "Messi captained Argentina to the 2022 World Cup title in Qatar, beating France on penalties in the final."),
    Fact("⚽ Messi", "Messi won the Copa America with Argentina in 2021, his first major trophy with the national team."),
    Fact("⚽ Messi", "Messi left Barcelona in 2021 after about two decades at the club and joined Paris Saint-Germain."),
    Fact("⚽ Messi", "Messi joined Inter Miami in 2023 and won the Leagues Cup with the club that same year."),
    Fact("⚽ Ronaldo", "Cristiano Ronaldo was born in Funchal, on the Portuguese island of Madeira, in 1985."),
    Fact("⚽ Ronaldo", "Ronaldo began his senior career at Sporting CP before joining Manchester United in 2003."),
    Fact("⚽ Ronaldo", "Ronaldo has won the Champions League five times, once with Manchester United and four times with Real Madrid."),
    Fact("⚽ Ronaldo", "Ronaldo captained Portugal to victory at Euro 2016, the country's first major international trophy."),
    Fact("⚽ Ronaldo", "Ronaldo is the all-time top scorer in the history of Real Madrid."),
    Fact("⚽ Ronaldo", "Ronaldo has won the Ballon d'Or five times."),
    Fact("⚽ Ronaldo", "Ronaldo is the top scorer in men's international football, with more than 130 goals for Portugal."),
    Fact("⚽ Ronaldo", "In 2022, Ronaldo became the first man to score in five different World Cups."),
    Fact("⚽ Neymar", "Neymar's full name is Neymar da Silva Santos Junior, and he was born in Mogi das Cruzes, Brazil, in 1992."),
    Fact("⚽ Neymar", "Neymar began his professional career at Santos, the club where the great Pele became a legend."),
    Fact("⚽ Neymar", "Neymar won the Copa Libertadores with Santos in 2011."),
    Fact("⚽ Neymar", "After joining Barcelona in 2013, Neymar formed a famous attack with Lionel Messi and Luis Suarez, known as MSN."),
    Fact("⚽ Neymar", "In 2017, Neymar joined Paris Saint-Germain for 222 million euros, a world record transfer fee at the time."),
    Fact("⚽ Neymar", "Neymar won Olympic gold with Brazil at the 2016 Rio Olympics, scoring the winning penalty in the final shootout."),
    Fact("⚽ Neymar", "Neymar is Brazil's all-time top scorer, passing the great Pele's record in 2023."),
    Fact("⚽ Neymar", "Neymar joined the Saudi club Al Hilal in 2023, and in 2025 he returned to his first club, Santos."),
    Fact("🎥 Hollywood", "The first film to win the top prize at the Oscars, in 1929, was Wings, a silent movie about war pilots."),
    Fact("🎥 Hollywood", "Titanic won 11 Oscars in 1998, a record it shares with Ben-Hur and The Return of the King."),
    Fact("🎥 Hollywood", "Jurassic Park, from 1993, brought its dinosaurs to life with a mix of real-size puppets and early computer graphics."),
    Fact("🎥 Hollywood", "George Lucas kept the merchandising rights to Star Wars, and toys and products later earned him a fortune."),
    Fact("🎥 Hollywood", "Oppenheimer won seven Oscars in 2024, including Best Picture."),
    Fact("🎥 Hollywood", "James Cameron directed both Titanic and Avatar, two of the highest-earning films of all time."),
    Fact("🎥 Hollywood", "The famous Hollywood sign first read Hollywoodland, and it was an advertisement for a housing development."),
    Fact("🎥 Hollywood", "Walt Disney won more Academy Awards than any other person, with 22 competitive Oscars."),
    Fact("🧠 GK", "What is the capital of Australia?", "Canberra", "🧠 Quiz time"),
    Fact("🧠 GK", "Which planet is known as the Red Planet?", "Mars", "🧠 Quiz time"),
    Fact("🧠 GK", "How many continents are there on Earth?", "Seven", "🧠 Quiz time"),
    Fact("🧠 GK", "Who wrote the Indian national anthem, Jana Gana Mana?", "Rabindranath Tagore", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the largest ocean on Earth?", "The Pacific Ocean", "🧠 Quiz time"),
    Fact("🧠 GK", "Which gas do plants take in from the air for photosynthesis?", "Carbon dioxide", "🧠 Quiz time"),
    Fact("🧠 GK", "Who painted the Mona Lisa?", "Leonardo da Vinci", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the national animal of India?", "The tiger", "🧠 Quiz time"),
    Fact("🧠 GK", "How many players does one football team have on the field?", "Eleven", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the hardest natural substance on Earth?", "Diamond", "🧠 Quiz time"),
    Fact("🧠 GK", "Which is the smallest planet in our solar system?", "Mercury", "🧠 Quiz time"),
    Fact("🧠 GK", "Who was the first person to walk on the Moon?", "Neil Armstrong, in 1969", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the currency of Japan?", "The yen", "🧠 Quiz time"),
    Fact("🧠 GK", "Which mountain is the tallest above sea level?", "Mount Everest", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the chemical formula of water?", "H2O", "🧠 Quiz time"),
    Fact("🧠 GK", "Who is known as the Father of the Nation in India?", "Mahatma Gandhi", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the largest mammal in the world?", "The blue whale", "🧠 Quiz time"),
    Fact("🧠 GK", "How many bones does an adult human body have?", "206", "🧠 Quiz time"),
    Fact("🧠 GK", "Which country gave the Statue of Liberty to the United States?", "France", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the largest desert in the world?", "Antarctica", "🧠 Quiz time"),
    Fact("🫀 Human body", "Your skin is the largest organ of your body."),
    Fact("🫀 Human body", "The lining of your stomach is replaced every few days, so the acid inside does not harm it."),
    Fact("🫀 Human body", "Any two people share about 99.9 percent of their DNA."),
    Fact("🫀 Human body", "The smallest bone in your body is in your ear. It is about 3 millimetres long."),
    Fact("🫀 Human body", "An adult has about five litres of blood in the body."),
    Fact("🫀 Human body", "Fingernails grow faster than toenails."),
    Fact("🫀 Human body", "Your liver can regrow part of itself after it is damaged."),
    Fact("🫀 Human body", "A baby's kneecaps start out as soft cartilage and turn to bone later in childhood."),
        Fact("⚽ Messi", "As a boy, Messi was nicknamed La Pulga, which means the Flea, because he was so small."),
    Fact("⚽ Messi", "Messi won the Golden Ball as the best player of the World Cup twice, in 2014 and in 2022."),
    Fact("⚽ Messi", "Messi scored twice in the 2022 World Cup final against France, a game that ended 3-3 before Argentina won on penalties."),
    Fact("⚽ Messi", "In 2012, Messi scored 91 goals in a single calendar year, a world record."),
    Fact("⚽ Messi", "Messi won an Olympic gold medal with Argentina at the 2008 Beijing Games."),
    Fact("⚽ Messi", "Messi won the 2005 Under-20 World Cup with Argentina and was named the best player of the tournament."),
    Fact("⚽ Messi", "Messi wore the numbers 30 and 19 at Barcelona before getting the famous number 10 shirt in 2008."),
    Fact("⚽ Messi", "By the 2022 World Cup, Messi had played in five different World Cups."),
    Fact("🔭 Scientists", "Marie Curie is the only person to win Nobel Prizes in two different sciences, Physics in 1903 and Chemistry in 1911."),
    Fact("🔭 Scientists", "Albert Einstein won the Nobel Prize in Physics in 1921 for explaining the photoelectric effect, not for relativity."),
    Fact("🔭 Scientists", "Isaac Newton explained gravity and the laws of motion in his book Principia, published in 1687."),
    Fact("🔭 Scientists", "In 1610, Galileo used a telescope to discover the four biggest moons of Jupiter."),
    Fact("🔭 Scientists", "Charles Darwin published On the Origin of Species in 1859, explaining evolution by natural selection."),
    Fact("🔭 Scientists", "C. V. Raman won the Nobel Prize in Physics in 1930 for discovering how light scatters, now called the Raman effect. He was the first Asian to win a Nobel in science."),
    Fact("🔭 Scientists", "The Indian astronomer Aryabhata, around the year 499, wrote that the Earth spins on its axis. India's first satellite was named after him."),
    Fact("🔭 Scientists", "Srinivasa Ramanujan, a self-taught Indian mathematician, made amazing discoveries in number theory and worked with G. H. Hardy at Cambridge."),
    Fact("🔭 Scientists", "A. P. J. Abdul Kalam helped develop India's missile and space launch programmes and later became the President of India."),
    Fact("🔭 Scientists", "Nikola Tesla's work on alternating current helped make today's electric power grids possible."),
    Fact("🔭 Scientists", "Alexander Fleming discovered penicillin in 1928 after noticing that mould on a dish had killed the bacteria around it."),
    Fact("🔭 Scientists", "Louis Pasteur created the process of pasteurisation and helped develop vaccines against rabies and anthrax."),
    Fact("🔭 Scientists", "Stephen Hawking showed that black holes can slowly give off radiation, now called Hawking radiation, and wrote A Brief History of Time."),
    Fact("🔭 Scientists", "Jagadish Chandra Bose, an Indian scientist, showed that plants respond to touch and other stimuli, and he did early work on radio waves."),
    Fact("🔭 Scientists", "Alan Turing helped break the Enigma code in the Second World War and laid the foundations of computer science."),
    Fact("🔭 Scientists", "Rosalind Franklin's X-ray photograph of DNA, known as Photo 51, helped scientists work out its double helix shape."),
    Fact("🔭 Scientists", "Dmitri Mendeleev built the periodic table in 1869 and left gaps for elements that had not been discovered yet."),
    Fact("🔭 Scientists", "Vikram Sarabhai is called the father of India's space programme, and he helped set up ISRO."),
    Fact("🌡️ Climate", "The oceans have absorbed more than 90 percent of the extra heat trapped by greenhouse gases."),
    Fact("🌡️ Climate", "The level of carbon dioxide in the air today is higher than at any time in at least 800,000 years."),
    Fact("🌡️ Climate", "The ozone layer has been slowly recovering since 1987, when the Montreal Protocol banned chemicals that were damaging it."),
    Fact("🌡️ Climate", "Without the natural greenhouse effect, Earth's average temperature would be around minus 18 degrees Celsius."),
    Fact("🌡️ Climate", "Ice sheets and glaciers hold about 69 percent of all the fresh water on Earth."),
    Fact("🌡️ Climate", "Sea levels have risen by about 20 centimetres since the start of the 1900s."),
    Fact("🌡️ Climate", "The Arctic is warming much faster than the rest of the planet, which is why its sea ice is shrinking."),
    Fact("🌡️ Climate", "Mangrove forests protect coasts from storms and store large amounts of carbon."),
    Fact("🌡️ Climate", "Earth's average temperature has risen by more than 1 degree Celsius since the late 1800s."),
    Fact("🌡️ Climate", "India and France launched the International Solar Alliance in 2015 to help countries use more solar power."),
    Fact("🌡️ Climate", "Coral reefs cover less than 1 percent of the ocean floor, but about a quarter of all sea creatures depend on them."),
    Fact("🏝️ Islands", "Greenland is the largest island in the world that is not a continent."),
    Fact("🏝️ Islands", "Indonesia is made up of more than 17,000 islands."),
    Fact("🏝️ Islands", "The Andaman and Nicobar Islands, in the Bay of Bengal, are part of India and include more than 500 islands."),
    Fact("🏝️ Islands", "Lakshadweep, made of coral islands in the Arabian Sea, is the smallest union territory of India."),
    Fact("🏝️ Islands", "The Maldives is the lowest-lying country in the world. Its highest natural point is only about 2.4 metres above sea level."),
    Fact("🏝️ Islands", "Majuli, in the Brahmaputra River in Assam, has been recognised as the largest river island in the world."),
    Fact("🏝️ Islands", "Iceland sits on the Mid-Atlantic Ridge, where two giant plates of the Earth's crust are slowly pulling apart."),
    Fact("🏝️ Islands", "Measured from its base on the ocean floor, Hawaii's Mauna Kea is taller than Mount Everest."),
    Fact("🏝️ Islands", "Madagascar is home to many animals found nowhere else on Earth, including lemurs."),
    Fact("🌊 Oceans", "Oceans cover about 71 percent of the surface of the Earth."),
    Fact("🌊 Oceans", "The Pacific Ocean holds about half of all the ocean water on the planet."),
    Fact("🌊 Oceans", "The deepest known spot in the ocean, Challenger Deep in the Mariana Trench, is nearly 11 kilometres down."),
    Fact("🌊 Oceans", "Most of the ocean floor has still not been mapped in detail."),
    Fact("🌊 Oceans", "The Indian Ocean is the only ocean named after a country."),
    Fact("🌊 Oceans", "The Great Barrier Reef, off the coast of Australia, is the largest coral reef system in the world."),
    Fact("🌊 Oceans", "Tiny ocean plants called phytoplankton produce about half of the oxygen we breathe."),
    Fact("🌊 Oceans", "Sea water is about 3.5 percent salt on average."),
    Fact("🌊 Oceans", "The Gulf Stream carries warm water across the Atlantic Ocean and keeps parts of Europe milder than they would otherwise be."),
    Fact("🌊 Oceans", "Tides are caused mainly by the Moon's gravity pulling on the oceans."),
    Fact("🌋 Earth", "The tectonic plates that make up Earth's crust move about as fast as your fingernails grow."),
    Fact("🌋 Earth", "About 75 percent of the world's volcanoes are along the Ring of Fire around the Pacific Ocean."),
    Fact("🌋 Earth", "Mount Everest is about 8,849 metres tall."),
    Fact("🌋 Earth", "Scientists still debate whether the Nile or the Amazon is the longest river in the world."),
    Fact("🌋 Earth", "Antarctica holds about 90 percent of all the ice on Earth."),
    Fact("🌋 Earth", "Earth is the only planet we know of that has liquid water on its surface."),
    Fact("🧠 GK", "Which scientist discovered penicillin?", "Alexander Fleming", "🧠 Quiz time"),
    Fact("🧠 GK", "Who developed the theory of general relativity?", "Albert Einstein", "🧠 Quiz time"),
    Fact("🧠 GK", "Which Indian scientist won the Nobel Prize in Physics in 1930?", "C. V. Raman", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the deepest known part of the world's oceans?", "Challenger Deep in the Mariana Trench", "🧠 Quiz time"),
    Fact("🧠 GK", "What is the largest island in the world that is not a continent?", "Greenland", "🧠 Quiz time"),
    Fact("🧠 GK", "Which gas released by burning fuels is the main driver of climate change?", "Carbon dioxide", "🧠 Quiz time"),
    
    
)

fun nextFact(current: Int): Int {
    var n = FACTS.indices.random()
    if (n == current && FACTS.size > 1) n = (current + 1) % FACTS.size
    return n
}

@Composable
fun FactCard(fact: Fact, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    var revealed by remember(fact) { mutableStateOf(false) }
    val isQuiz = fact.answer.isNotEmpty()
    val tappable = onClick != null || isQuiz

    LaunchedEffect(fact) {
        if (isQuiz && onClick == null) {
            delay(6000)
            revealed = true
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF2A2266))
            .then(
                if (tappable) Modifier.clickable {
                    if (isQuiz && !revealed) {
                        revealed = true
                    } else {
                        onClick?.invoke()
                    }
                } else Modifier
            )
            .padding(20.dp)
    ) {
        Text(
            fact.header + "  ·  " + fact.topic,
            color = Color(0xFF7ED957), fontSize = 13.sp, fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(10.dp))
        Text(fact.text, color = Color.White, fontSize = 16.sp, lineHeight = 23.sp)
        if (isQuiz) {
            Spacer(Modifier.height(12.dp))
            if (revealed) {
                Text(
                    "✅ " + fact.answer,
                    color = Color(0xFF7ED957), fontSize = 17.sp, fontWeight = FontWeight.Bold
                )
                if (onClick != null) {
                    Spacer(Modifier.height(8.dp))
                    Text("Tap for another one", color = Color(0xFFB9B2F5), fontSize = 12.sp)
                }
            } else {
                Text("Tap to reveal the answer", color = Color(0xFFB9B2F5), fontSize = 12.sp)
            }
        } else if (onClick != null) {
            Spacer(Modifier.height(10.dp))
            Text("Tap for another fact", color = Color(0xFFB9B2F5), fontSize = 12.sp)
        }
    }
}
