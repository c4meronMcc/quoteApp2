**Mobile Development 2025/26 Portfolio**
# Overview

Student ID: `24067465`

This application is a mobile decision-support tool for off-site project managers evaluating material supplier quotes.
My motivation stems from observing severe inefficiencies in my mum's screen printing business, where manually transcribing
varied, unstructured tenders into Excel takes hours. A mobile solution is essential because these professionals operate
away from desks but must make rapid financial commitments; choosing the wrong supplier risks crippling profit margins or
halting production entirely.

Users import digital PDF quotes directly from their device. Crucially for field environments with poor connectivity, the
text extraction and parsing pipeline operates entirely offline. It processes the data to populate a mobile-optimised
dashboard, enabling clean, side-by-side comparisons of critical variables like total amount, date, reference number and 
supplier name in one centralised view.

Users then define their priorities, such as 'lowest cost'. prompting an integrated recommendation function, which analyses 
the JSON dataset and highlights the optimal choice. Finally, the app generates a formatted summary PDF of the comparison, 
explicitly featuring the recommendation, ready to be shared with colleagues for immediate sign-off.