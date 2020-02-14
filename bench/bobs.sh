source ~/.bashrc

./change-bs.sh 1000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 2000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 4000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 8000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 16000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 32000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 64000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 128000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 512000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 1024000000
hidoop bench gb 2 &>> bobs_results

./change-bs.sh 2048000000
hidoop bench gb 2 &>> bobs_results