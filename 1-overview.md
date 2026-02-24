**Mobile Development 2025/26 Portfolio**
# Overview

Student ID: `24067465`

This application is a decision-support tool for field-based project managers, such as construction 
site leads, who are evaluating supplier quotes while away from office infrastructure. In 
high-pressure environments, quotes often arrive as fragmented physical documents or inconsistent 
digital attachments. The app enables immediate data capture via the device camera or the Android
Storage Access Framework. By leveraging the Gemini API, the system extracts key variables such as 
unit costs, tax, and delivery lead times from varied documents layouts and converts them into a 
structured dataset from immediate comparison.

The core functionality is a mobile-optimised comparison dashboard that automatically evaluates 
quotes against one another. Designed for constrained screen space, it highlights the optimal 
procurement option based on user-defined priorities, such as lowest total cost or fastest 
fulfilment.

The project addresses the financial impact of decision latency and manual transcription errors in 
procurement workflows. Comparing complex tenders typically requires desktop spreadsheets which are
impractical on site and increase operational risk. This mobile solution removes the need for manual 
data entry, accelerating procurement decisions at the point of receipt. By transforming, the 
application reduces delays and supports accurate, data-driven financial commitments regardless of 
location.

<!-- 
Comments from SG

Interesting choice! New one to me. I think there's a bit of work to be done refining this. I have a few comments:

- I can see why a mobile-based app is going to be especially useful for this context, the question of 'why a mobile app' is, I think, clearly answered here.

- Whats your motivation for building this app? I have a clear idea of who the app is target at, but I don't get a sense at all of why you think that this is a good choice given either your personal motivations or the assignment brief.

- You have a few ChatGPTisms here. LLMs can be useful for developing ideas, but I don't think their style is suited to this context/task. For example, when you write "By transforming, the application reduces delays and supports accurate, data-driven financial commitments regardless of location." This doesn't really convey anything other than "the app's gonna be great!" And so it's not going to contribute to your mark here.

- You could save some space to say a little more about what the app is actually going to do. I don't have a sense from this of what to expect when I open the app up. You don't need to exhaustively list, eg, the different activities that the app's going to have, but you need to give some impression of what folks will be able to do with the app.

- Is an LLM the right tool for parsing and organising numerical data? Leaving that aside, you're proposing to do a lot here. An API-based app that takes documents, analyses them and then presents the outputs in a tractable way. But this is a bit risky - what if you can't get this workflow going? Can you think of an MVP here that might be a safer place to aim for as you start the project, and then look to expand once you have a solid base?

-->