# LazyNotes

LazyNotes is a mobile lecture companion app that records audio, transcribes it into text, and generates AI-powered summaries with timestamps. It helps students review lectures efficiently by combining recording, transcription, and summarization in one place.

## Team Members
| Name | Email | GitHub |
|------|--------|--------|
| James Yin | xyin63@wisc.edu | xyinjame |
| Tim Yang | zyang785@wisc.edu | TimShakespeare |
| Eric Wang | ewang65@wisc.edu | NotAsura |
| Yawen Zhang | yzhang2863@wisc.edu | yzhang2863 |

## Overview
LazyNotes allows users to record lectures through their mobile device, automatically transcribe the audio into text using a speech-to-text API, and generate structured summaries using a large language model. Users can replay sections of a lecture, browse summaries, and organize past recordings.

## Core Features
- Audio recording with start, pause, and stop controls
- Automatic transcription with timestamps
- AI-generated summaries and key points
- Local storage of lectures and transcripts
- Simple and clean user interface built with Jetpack Compose

## System Overview

### Mobile (Android)
- Records audio using a foreground service and saves files locally
- Displays recordings, transcripts, and summaries through a Compose-based UI
- Stores metadata and user preferences with Room and DataStore

### Server-Side
- API backend handles communication with external transcription and summarization services
- Transcription module processes audio using an ASR model (Whisper, Google STT, etc.)
- Summarization module generates structured text summaries via a large language model (e.g., GPT)


## Tech Stack
- Kotlin, Jetpack Compose
- Room Database, WorkManager
- Whisper / AssemblyAI / Google STT for transcription
- OpenAI GPT for summarization

## Milestones & Roles
Milestone 1  
Project Coordinator:  
Observer:  
