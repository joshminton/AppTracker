# Tackling problematic smartphone usage with on-screen usage feedback
## _Or 'helping people use their phones less by giving them a sense of how much they've used their device'_

AppTracker is an app developed as part of an MSc Human Computer Interaction dissertation, which builds on research into problematic smartphone usage and techniques. A survey of existing literature and solutions found that while _tracking_ of phone use is now a common and readily available feature, this tracking data is not always immediately available to the user. AppTracker attempts to remove the need for frustrating interventions such as outright restriction of app usage, by instead giving the user a continuous, subliminal awareness of their smartphone usage, encouraging mindfulness.

This awareness is achieved by displaying an 'edge glow' on screen, at all times. It changes into two dimensions -- the amount of the screen edge it covers, and the intensity/colour/brightness of the glow -- to communicate two distinct quantities; the user's progress towards a daily target of usage, and particularly intense usage sessions. The latter builds on a finding from previous research that these long, mindless, habitual use cases are perceived as particularly unsatisfying by users, giving a sense of a lack of autonomy.

In short, the edge of the screen gets more and more red when a problematic app is used for a longer period of time, and the more these apps are used in a single day, the more of the screen the glow will surround. The app therefore asks users to select apps they wish to use less of, and to set a daily usage target.

This is a _targeted_ and _scaled_ approach, aimed at reducing the frustration experienced by the user as a result of using AppTracker, as frustration is the main cause of deactivating similar intervention software. Firstly, it is _targeted_ in that only apps the user has said they want to use less of are affected -- so the user has already bought into this. Useful functions of the phone remain fully accessible. Secondly, the glow _scales_ to the amount of usage, being barely noticeable after a short time, but gradually becoming more and more obtrusive.

More on the design of the app and the theory behind it can be found in the [project report](ProjectReport.pdf).

### Screenshots

### Trial and results
After the app was developed, a two week study was completed by fourteen participants who used the app on their phones. Anonymised usage data was collected using Firebase. The results were very pleasing: analysis found that average daily usage of apps each user chose to use less reduced by 25.1% on average for the period after installing AppTracker, which was a statistically significant result. Additionally, average daily usage of apps the user did not choose to use less reduced by only 1.7%. 10 of 12 resondents to a post-trial questionnaire felt the app helped them manage their smartphone usage to at least a decent extent.

