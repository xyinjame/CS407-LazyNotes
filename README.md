# LazyNotes

LazyNotes is a mobile lecture companion app that records audio, transcribes it into text, and generates AI-powered summaries and flashcards. It helps students review lectures efficiently by combining transcription, summarization, note organization, and study tools in one place.

## Team Members
| Name | Email | GitHub |
|------|--------|--------|
| James Yin | xyin63@wisc.edu | xyinjame |
| Tim Yang | zyang785@wisc.edu | TimShakespeare |
| Eric Wang | ewang65@wisc.edu | ewang2005 |
| Yawen Zhang | yzhang2863@wisc.edu | yzhang2863 |

## Overview
LazyNotes allows users to record audio, upload it to Firebase Storage, and receive an automated transcript via the Fireflies transcription API. The app then generates an AI-powered summary of the transcript using Perplexity. Users can organize notes into folders, rename/move/delete notes, and convert summaries/transcripts into AI-generated flashcards for studying.

## Core Features
- Audio Recording: Start, pause, resume, and stop recording.
- Automatic Transcription via Fireflies.
- AI Summaries generated through Perplexity.
- AI Flashcards.
- Folder + Note Management:
  - Create/delete folders
  - Rename/delete/move notes
  - Folder sorting: alphabetical or recently edited
  - Expandable/collapsible folder list
- Local Persistence using Room + DataStore
- Firebase Storage for audio uploads
- Clean UI built with Jetpack Compose

## System Overview

### Mobile (Android)
- Records audio and uploads it to Firebase Storage.
- Polls Fireflies for transcript completion.
- Generates summaries through Perplexity.
- Creates flashcards using the transcript content.
- Stores all notes and folders locally using Room.
- Saves user preferences (folder layout, default view mode) using DataStore.
- Fully Jetpack Compose UI.

### Server-Side
- Fireflies API for speech-to-text transcription.
- Perplexity API for:
  - Text summarization
  - Flashcard generation

## Tech Stack
- Language: Kotlin
- UI: Jetpack Compose
- Local Storage: Room, DataStore
- Cloud: Firebase Storage
- Transcription: Fireflies API
- Summaries & Flashcards: Perplexity API

## Milestones & Roles
Milestone 1    
Project Coordinator: James Yin  
Observer: Eric Wang  
Milestone 2  
Project Coordinator: Yawen Zhang  
Observer: James Yin  
Milestone 3  
Project Coordinator: Eric Wang  
Observer: Tim Yang  
